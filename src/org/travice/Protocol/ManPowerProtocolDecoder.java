
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class ManPowerProtocolDecoder extends BaseProtocolDecoder {

    public ManPowerProtocolDecoder(ManPowerProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("simei:")
            .number("(d+),")                     // imei
            .expression("[^,]*,[^,]*,")
            .expression("([^,]*),")              // status
            .number("d+,d+,d+.?d*,")
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(dd)(dd.dddd),")            // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.dddd),")           // longitude
            .expression("([EW])?,")
            .number("(d+.?d*),")                 // speed
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.set(Position.KEY_STATUS, parser.next());

        position.setTime(parser.nextDateTime());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));

        return position;
    }

}
