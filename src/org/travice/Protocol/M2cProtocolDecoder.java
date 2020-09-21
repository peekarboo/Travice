
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class M2cProtocolDecoder extends BaseProtocolDecoder {

    public M2cProtocolDecoder(M2cProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("#M2C,")
            .expression("[^,]+,")                // model
            .expression("[^,]+,")                // firmware
            .number("d+,")                       // protocol
            .number("(d+),")                     // imei
            .number("(d+),")                     // index
            .expression("([LH]),")               // archive
            .number("d+,")                       // priority
            .number("(d+),")                     // event
            .number("(dd)(dd)(dd),")             // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+),")                   // altitude
            .number("(d+),")                     // course
            .number("(d+.d+),")                  // speed
            .number("(d+),")                     // satellites
            .number("(d+),")                     // odometer
            .number("(d+),")                     // input
            .number("(d+),")                     // output
            .number("(d+),")                     // power
            .number("(d+),")                     // battery
            .number("(d+),")                     // adc 1
            .number("(d+),")                     // adc 2
            .number("(d+.?d*),")                 // temperature
            .any()
            .compile();

    private Position decodePosition(Channel channel, SocketAddress remoteAddress, String line) {

        Parser parser = new Parser(PATTERN, line);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.set(Position.KEY_INDEX, parser.nextInt());

        if (parser.next().equals("H")) {
            position.set(Position.KEY_ARCHIVE, true);
        }

        position.set(Position.KEY_EVENT, parser.nextInt());

        position.setValid(true);
        position.setTime(parser.nextDateTime());
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setAltitude(parser.nextInt());
        position.setCourse(parser.nextInt());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_ODOMETER, parser.nextLong());
        position.set(Position.KEY_INPUT, parser.nextInt());
        position.set(Position.KEY_OUTPUT, parser.nextInt());
        position.set(Position.KEY_POWER, parser.nextInt() * 0.001);
        position.set(Position.KEY_BATTERY, parser.nextInt() * 0.001);
        position.set(Position.PREFIX_ADC + 1, parser.nextInt());
        position.set(Position.PREFIX_ADC + 2, parser.nextInt());
        position.set(Position.PREFIX_TEMP + 1, parser.nextDouble());

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        sentence = sentence.substring(1); // remove start symbol

        List<Position> positions = new LinkedList<>();
        for (String line : sentence.split("\r\n")) {
            if (!line.isEmpty()) {
                Position position = decodePosition(channel, remoteAddress, line);
                if (position != null) {
                    positions.add(position);
                }
            }
        }

        return positions;
    }

}
