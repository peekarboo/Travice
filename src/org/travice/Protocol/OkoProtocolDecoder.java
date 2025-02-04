
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

public class OkoProtocolDecoder extends BaseProtocolDecoder {

    public OkoProtocolDecoder(OkoProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("{")
            .number("(d{15}),").optional()       // imei
            .number("(dd)(dd)(dd).d+,")          // time
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(d+),")                     // satellites
            .number("(d+.d+),")                  // adc
            .number("(xx),")                     // event
            .number("(d+.d+),")                  // power
            .number("d,")                        // memory status
            .number("(xx)")                      // io
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession;
        if (parser.hasNext()) {
            deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        } else {
            deviceSession = getDeviceSession(channel, remoteAddress);
        }
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
        position.set(Position.KEY_EVENT, parser.next());
        position.set(Position.KEY_POWER, parser.nextDouble());
        position.set(Position.KEY_INPUT, parser.nextHexInt());

        return position;
    }

}
