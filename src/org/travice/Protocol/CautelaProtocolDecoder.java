
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class CautelaProtocolDecoder extends BaseProtocolDecoder {

    public CautelaProtocolDecoder(CautelaProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+),")                     // type
            .number("(d+),")                     // imei
            .number("(dd),(dd),(dd),")           // date (ddmmyy)
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(dd)(dd),")                 // time (hhmm)
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        parser.next(); // type

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder();
        dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());

        position.setValid(true);
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());

        dateBuilder.setHour(parser.nextInt()).setMinute(parser.nextInt());
        position.setTime(dateBuilder.getDate());

        return position;
    }

}
