
package org.travice.protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.Parser.CoordinateFormat;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

public class CarcellProtocolDecoder extends BaseProtocolDecoder {

    public CarcellProtocolDecoder(CarcellProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("([$%])")                // memory flag
            .number("(d+),")                     // imei
            .groupBegin()
            .number("([NS])(dd)(dd).(dddd),")    // latitude
            .number("([EW])(ddd)(dd).(dddd),")   // longitude
            .or()
            .text("CEL,")
            .number("([NS])(d+.d+),")            // latitude
            .number("([EW])(d+.d+),")            // longitude
            .groupEnd()
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .groupBegin()
            .number("([-+]ddd)([-+]ddd)([-+]ddd),") // x,y,z
            .or()
            .number("(d+),")                     // accel
            .groupEnd()
            .number("(d+),")                     // battery
            .number("(d+),")                     // csq
            .number("(d),")                      // jamming
            .number("(d+),")                     // hdop
            .expression("([CG]),?")              // clock type
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d),")                      // block
            .number("(d),")                      // ignition
            .groupBegin()
            .number("(d),")                      // cloned
            .expression("([AF])")                // panic
            .number("(d),")                      // painel
            .number("(d+),")                     // battery voltage
            .or()
            .number("(dd),")                     // time until delivery
            .expression("([AF])")                // panic
            .number("(d),")                      // aux
            .number("(d{2,4}),")                 // battery voltage
            .number("(d{20}),")                  // ccid
            .groupEnd()
            .number("(xx)")                      // crc
            .any()                               // full format
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.set(Position.KEY_ARCHIVE, parser.next().equals("%"));
        position.setValid(true);

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        if (parser.hasNext(8)) {
            position.setLatitude(parser.nextCoordinate(CoordinateFormat.HEM_DEG_MIN_MIN));
            position.setLongitude(parser.nextCoordinate(CoordinateFormat.HEM_DEG_MIN_MIN));
        }

        if (parser.hasNext(4)) {
            position.setLatitude(parser.nextCoordinate(CoordinateFormat.HEM_DEG));
            position.setLongitude(parser.nextCoordinate(CoordinateFormat.HEM_DEG));
        }

        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
        position.setCourse(parser.nextInt(0));

        if (parser.hasNext(3)) {
            position.set("x", parser.nextInt(0));
            position.set("y", parser.nextInt(0));
            position.set("z", parser.nextInt(0));
        }

        if (parser.hasNext(1)) {
            position.set(Position.KEY_ACCELERATION, parser.nextInt(0));
        }

        Double internalBattery = (parser.nextDouble(0) + 100d) * 0.0294d;
        position.set(Position.KEY_BATTERY, internalBattery);
        position.set(Position.KEY_RSSI, parser.nextInt(0));
        position.set("jamming", parser.next().equals("1"));
        position.set(Position.KEY_GPS, parser.nextInt(0));

        position.set("clockType", parser.next());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.set("blocked", parser.next().equals("1"));
        position.set(Position.KEY_IGNITION, parser.next().equals("1"));

        if (parser.hasNext(4)) {
            position.set("cloned", parser.next().equals("1"));

            parser.next(); // panic button status

            String painelStatus = parser.next();
            if (painelStatus.equals("1")) {
                position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
            }
            position.set("painel", painelStatus.equals("2"));

            Double mainVoltage = parser.nextDouble(0) / 100d;
            position.set(Position.KEY_POWER, mainVoltage);
        }

        if (parser.hasNext(5)) {
            position.set("timeUntilDelivery", parser.nextInt(0));
            parser.next(); // panic button status
            position.set(Position.KEY_INPUT, parser.next());

            Double mainVoltage = parser.nextDouble(0) / 100d;
            position.set(Position.KEY_POWER, mainVoltage);

            position.set("iccid", parser.next());
        }

        return position;
    }

}
