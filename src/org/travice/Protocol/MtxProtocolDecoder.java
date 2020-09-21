
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class MtxProtocolDecoder extends BaseProtocolDecoder {

    public MtxProtocolDecoder(MtxProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("#MTX,")
            .number("(d+),")                     // imei
            .number("(dddd)(dd)(dd),")           // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+.?d*),")                 // speed
            .number("(d+),")                     // course
            .number("(d+.?d*),")                 // odometer
            .groupBegin()
            .number("d+")
            .or()
            .text("X")
            .groupEnd()
            .text(",")
            .expression("(?:[01]|X),")
            .expression("([01]+),")              // input
            .expression("([01]+),")              // output
            .number("(d+),")                     // adc1
            .number("(d+)")                      // adc2
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage("#ACK", remoteAddress));
        }

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

        position.setTime(parser.nextDateTime());

        position.setValid(true);
        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_ODOMETER, parser.nextDouble(0) * 1000);
        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_OUTPUT, parser.next());
        position.set(Position.PREFIX_ADC + 1, parser.next());
        position.set(Position.PREFIX_ADC + 2, parser.next());

        return position;
    }

}
