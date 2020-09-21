
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;

public class CradlepointProtocolDecoder extends BaseProtocolDecoder {

    public CradlepointProtocolDecoder(CradlepointProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("([^,]+),")              // id
            .number("(d{1,6}),")                 // time (hhmmss)
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([EW]),")
            .number("(d+.d+)?,")                 // speed
            .number("(d+.d+)?,")                 // course
            .expression("([^,]+)?,")             // carrier
            .expression("([^,]+)?,")             // serdis
            .number("(-?d+)?,")                  // rsrp
            .number("(-?d+)?,")                  // rssi
            .number("(-?d+)?,")                  // rsrq
            .expression("([^,]+)?,")             // ecio
            .expression("([^,]+)?")              // wan ip
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        int time = parser.nextInt();
        DateBuilder dateBuilder = new DateBuilder(new Date());
        dateBuilder.setHour(time / 100 / 100);
        dateBuilder.setMinute(time / 100 % 100);
        dateBuilder.setSecond(time % 100);
        position.setTime(dateBuilder.getDate());

        position.setValid(true);
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set("carrid", parser.next());
        position.set("serdis", parser.next());
        position.set("rsrp", parser.nextInt());
        position.set(Position.KEY_RSSI, parser.nextInt());
        position.set("rsrq", parser.nextInt());
        position.set("ecio", parser.next());

        return position;
    }

}
