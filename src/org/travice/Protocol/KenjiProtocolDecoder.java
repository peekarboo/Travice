
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class KenjiProtocolDecoder extends BaseProtocolDecoder {

    public KenjiProtocolDecoder(KenjiProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text(">")
            .number("C(d{6}),")                  // device id
            .number("M(x{6}),")                  // alarm
            .number("O(x{4}),")                  // output
            .number("I(x{4}),")                  // input
            .number("D(dd)(dd)(dd),")            // time (hhmmss)
            .expression("([AV]),")               // valid
            .number("([NS])(dd)(dd.d+),")        // latitude
            .number("([EW])(ddd)(dd.d+),")       // longitude
            .number("T(d+.d+),")                 // speed
            .number("H(d+.d+),")                 // course
            .number("Y(dd)(dd)(dd),")            // date (ddmmyy)
            .number("G(d+)")                     // satellites
            .any()
            .compile();

    private String decodeAlarm(int value) {
        if (BitUtil.check(value, 2)) {
            return Position.ALARM_SOS;
        }
        if (BitUtil.check(value, 4)) {
            return Position.ALARM_LOW_BATTERY;
        }
        if (BitUtil.check(value, 6)) {
            return Position.ALARM_MOVEMENT;
        }
        if (BitUtil.check(value, 1) || BitUtil.check(value, 10) || BitUtil.check(value, 11)) {
            return Position.ALARM_VIBRATION;
        }

        return null;
    }

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

        position.set(Position.KEY_ALARM, decodeAlarm(parser.nextHexInt(0)));
        position.set(Position.KEY_OUTPUT, parser.nextHexInt(0));
        position.set(Position.KEY_INPUT, parser.nextHexInt(0));

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.setValid(parser.next().equals("A"));

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_SATELLITES, parser.nextInt(0));

        return position;
    }

}
