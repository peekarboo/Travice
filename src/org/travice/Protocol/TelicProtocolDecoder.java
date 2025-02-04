
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

public class TelicProtocolDecoder extends BaseProtocolDecoder {

    public TelicProtocolDecoder(TelicProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("dddd")
            .number("(d{6}|d{15})")              // device id
            .number("(d{1,2}),")                 // type
            .number("d{12},")                    // event time
            .number("d+,")
            .number("(dd)(dd)(dd)")              // date (ddmmyy)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .groupBegin()
            .number("(ddd)(dd)(dddd),")          // longitude
            .number("(dd)(dd)(dddd),")           // latitude
            .or()
            .number("(-?d+),")                   // longitude
            .number("(-?d+),")                   // latitude
            .groupEnd()
            .number("(d),")                      // validity
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+)?,")                    // satellites
            .expression("(?:[^,]*,){7}")
            .number("(d+),")                     // battery
            .any()
            .compile();

    private String decodeAlarm(int eventId) {

        switch (eventId) {
            case 1:
                return Position.ALARM_POWER_ON;
            case 2:
                return Position.ALARM_SOS;
            case 5:
                return Position.ALARM_POWER_OFF;
            case 7:
                return Position.ALARM_GEOFENCE_ENTER;
            case 8:
                return Position.ALARM_GEOFENCE_EXIT;
            case 22:
                return Position.ALARM_LOW_BATTERY;
            case 25:
                return Position.ALARM_MOVEMENT;
            default:
                return null;
        }
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

        int event = parser.nextInt(0);
        position.set(Position.KEY_EVENT, event);

        position.set(Position.KEY_ALARM, decodeAlarm(event));

        if (event == 11) {
            position.set(Position.KEY_IGNITION, true);
        } else if (event == 12) {
            position.set(Position.KEY_IGNITION, false);
        }

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        if (parser.hasNext(6)) {
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
        }

        if (parser.hasNext(2)) {
            position.setLongitude(parser.nextDouble(0) / 10000);
            position.setLatitude(parser.nextDouble(0) / 10000);
        }

        position.setValid(parser.nextInt(0) != 1);
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));

        if (parser.hasNext()) {
            position.set(Position.KEY_SATELLITES, parser.nextInt(0));
        }

        position.set(Position.KEY_BATTERY, parser.nextInt(0));

        return position;
    }

}
