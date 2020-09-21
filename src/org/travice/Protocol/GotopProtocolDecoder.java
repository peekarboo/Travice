
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class GotopProtocolDecoder extends BaseProtocolDecoder {

    public GotopProtocolDecoder(GotopProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+),")                     // imei
            .expression("[^,]+,")                // type
            .expression("([AV]),")               // validity
            .number("DATE:(dd)(dd)(dd),")        // date (yyddmm)
            .number("TIME:(dd)(dd)(dd),")        // time (hhmmss)
            .number("LAT:(d+.d+)([NS]),")        // latitude
            .number("LOT:(d+.d+)([EW]),")        // longitude
            .text("Speed:").number("(d+.d+),")   // speed
            .expression("([^,]+),")              // status
            .number("(d+)?")                     // course
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

        position.setValid(parser.next().equals("A"));

        position.setTime(parser.nextDateTime());

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));

        position.set(Position.KEY_STATUS, parser.next());

        position.setCourse(parser.nextDouble(0));

        return position;
    }

}
