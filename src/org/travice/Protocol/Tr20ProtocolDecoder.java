
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class Tr20ProtocolDecoder extends BaseProtocolDecoder {

    public Tr20ProtocolDecoder(Tr20Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_PING = new PatternBuilder()
            .text("%%")
            .expression("[^,]+,")
            .number("(d+)")
            .compile();

    private static final Pattern PATTERN_DATA = new PatternBuilder()
            .text("%%")
            .expression("([^,]+),")              // id
            .expression("([AL]),")               // validity
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([NS])")
            .number("(dd)(dd.d+)")               // latitude
            .expression("([EW])")
            .number("(ddd)(dd.d+),")             // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN_PING, (String) msg);
        if (parser.matches()) {
            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage(
                        "&&" + parser.next() + "\r\n", remoteAddress)); // keep-alive response
            }
            return null;
        }

        parser = new Parser(PATTERN_DATA, (String) msg);
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

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));

        return position;
    }

}
