
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

public class BoxProtocolDecoder extends BaseProtocolDecoder {

    public BoxProtocolDecoder(BoxProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("L,")
            .number("(dd)(dd)(dd)")              // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .text("G,")
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*),")                 // course
            .number("(d+.?d*),")                 // distance
            .number("(d+),")                     // event
            .number("(d+)")                      // status
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        if (sentence.startsWith("H,")) {

            int index = sentence.indexOf(',', 2) + 1;
            String id = sentence.substring(index, sentence.indexOf(',', index));
            getDeviceSession(channel, remoteAddress, id);

        } else if (sentence.startsWith("E,")) {

            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage("A," + sentence.substring(2) + "\r", remoteAddress));
            }

        } else if (sentence.startsWith("L,")) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            Parser parser = new Parser(PATTERN, sentence);
            if (!parser.matches()) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setTime(parser.nextDateTime());

            position.setLatitude(parser.nextDouble(0));
            position.setLongitude(parser.nextDouble(0));
            position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
            position.setCourse(parser.nextDouble(0));

            position.set(Position.KEY_ODOMETER_TRIP, parser.nextDouble(0) * 1000);
            position.set(Position.KEY_EVENT, parser.next());

            int status = parser.nextInt(0);
            position.setValid((status & 0x04) == 0);
            position.set(Position.KEY_STATUS, status);

            return position;
        }

        return null;
    }

}
