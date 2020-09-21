
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class HomtecsProtocolDecoder extends BaseProtocolDecoder {

    public HomtecsProtocolDecoder(HomtecsProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("([^_]+)")               // id
            .text("_R")
            .number("(x{8}),")                   // mac ending
            .number("(dd)(dd)(dd),")             // date (yymmdd)
            .number("(dd)(dd)(dd).d+,")          // time (hhmmss)
            .number("(d+),")                     // satellites
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(d),")                      // fix status
            .number("(d+.?d*)?,")                // hdop
            .number("(d+.?d*)?")                 // altitude
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String id = parser.next();
        String mac = parser.next();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id, id + "_R" + mac);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.YMD_HMS));

        position.set(Position.KEY_SATELLITES, parser.nextInt(0));

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.setValid(parser.nextInt(0) > 0);

        position.set(Position.KEY_HDOP, parser.nextDouble(0));

        position.setAltitude(parser.nextDouble(0));

        return position;
    }

}
