
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class AppelloProtocolDecoder extends BaseProtocolDecoder {

    public AppelloProtocolDecoder(AppelloProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("FOLLOWIT,")                   // brand
            .number("(d+),")                     // imei
            .groupBegin()
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd).?d*,")         // time (hhmmss)
            .or()
            .text("UTCTIME,")
            .groupEnd()
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // satellites
            .number("(-?d+),")                   // altitude
            .expression("([FL]),")               // gps state
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String imei = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        if (parser.hasNext(6)) {
            position.setTime(parser.nextDateTime());
        } else {
            getLastLocation(position, null);
        }

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_SATELLITES, parser.nextInt(0));

        position.setAltitude(parser.nextDouble(0));

        position.setValid(parser.next().equals("F"));

        return position;
    }

}
