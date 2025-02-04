
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class EasyTrackProtocolDecoder extends BaseProtocolDecoder {

    public EasyTrackProtocolDecoder(EasyTrackProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("*").expression("..,")         // manufacturer
            .number("(d+),")                     // imei
            .expression("([^,]{2}),")            // command
            .expression("([AV]),")               // validity
            .number("(xx)(xx)(xx),")             // date (yymmdd)
            .number("(xx)(xx)(xx),")             // time (hhmmss)
            .number("(x)(x{7}),")                // latitude
            .number("(x)(x{7}),")                // longitude
            .number("(x{4}),")                   // speed
            .number("(x{4}),")                   // course
            .number("(x{8}),")                   // status
            .number("(x+),")                     // signal
            .number("(d+),")                     // power
            .number("(x{4}),")                   // oil
            .number("(x+),?")                    // odometer
            .number("(d+)?")                     // altitude
            .any()
            .compile();

    private String decodeAlarm(long status) {
        if ((status & 0x02000000) != 0) {
            return Position.ALARM_GEOFENCE_ENTER;
        }
        if ((status & 0x04000000) != 0) {
            return Position.ALARM_GEOFENCE_EXIT;
        }
        if ((status & 0x08000000) != 0) {
            return Position.ALARM_LOW_BATTERY;
        }
        if ((status & 0x20000000) != 0) {
            return Position.ALARM_VIBRATION;
        }
        if ((status & 0x80000000) != 0) {
            return Position.ALARM_OVERSPEED;
        }
        if ((status & 0x00010000) != 0) {
            return Position.ALARM_SOS;
        }
        if ((status & 0x00040000) != 0) {
            return Position.ALARM_POWER_CUT;
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

        position.set(Position.KEY_COMMAND, parser.next());

        position.setValid(parser.next().equals("A"));

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0))
                .setTime(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0));
        position.setTime(dateBuilder.getDate());

        if (BitUtil.check(parser.nextHexInt(0), 3)) {
            position.setLatitude(-parser.nextHexInt(0) / 600000.0);
        } else {
            position.setLatitude(parser.nextHexInt(0) / 600000.0);
        }

        if (BitUtil.check(parser.nextHexInt(0), 3)) {
            position.setLongitude(-parser.nextHexInt(0) / 600000.0);
        } else {
            position.setLongitude(parser.nextHexInt(0) / 600000.0);
        }

        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextHexInt(0) / 100.0));
        position.setCourse(parser.nextHexInt(0) / 100.0);

        long status = parser.nextHexLong();
        position.set(Position.KEY_STATUS, status);
        position.set(Position.KEY_ALARM, decodeAlarm(status));

        position.set("signal", parser.next());
        position.set(Position.KEY_POWER, parser.nextDouble(0));
        position.set("oil", parser.nextHexInt(0));
        position.set(Position.KEY_ODOMETER, parser.nextHexInt(0) * 100);

        position.setAltitude(parser.nextDouble(0));

        return position;
    }

}
