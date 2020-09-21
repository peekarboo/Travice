
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

public class DwayProtocolDecoder extends BaseProtocolDecoder {

    public DwayProtocolDecoder(DwayProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("AA55,")
            .number("d+,")                       // index
            .number("(d+),")                     // imei
            .number("d+,")                       // type
            .number("(dd)(dd)(dd),")             // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+),")                   // altitude
            .number(" ?(d+.d+),")                // speed
            .number("(d+),")                     // course
            .number("([01]{4}),")                // input
            .number("([01]{4}),")                // output
            .number("([01]+),")                  // flags
            .number("(d+),")                     // battery
            .number("(d+),")                     // adc1
            .number("(d+),")                     // adc2
            .number("(d+)")                      // driver
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        if (sentence.equals("AA55,HB")) {
            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage("55AA,HB,OK\r\n", remoteAddress));
            }
            return null;
        }

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

        position.setValid(true);
        position.setTime(parser.nextDateTime());
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setAltitude(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_INPUT, parser.nextBinInt());
        position.set(Position.KEY_OUTPUT, parser.nextBinInt());

        position.set(Position.KEY_BATTERY, parser.nextInt() * 0.001);
        position.set(Position.PREFIX_ADC + 1, parser.nextInt() * 0.001);
        position.set(Position.PREFIX_ADC + 2, parser.nextInt() * 0.001);
        position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());

        return position;
    }

}
