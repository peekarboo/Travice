
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class TmgProtocolDecoder extends BaseProtocolDecoder {

    public TmgProtocolDecoder(TmgProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$")
            .expression("(...),")                // type
            .expression("[LH],").optional()      // history
            .number("(d+),")                     // imei
            .number("(dd)(dd)(dddd),")           // date (ddmmyyyy)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d),")                      // status
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*),")                 // course
            .groupBegin()
            .number("(-?d+.?d*),")               // altitude
            .number("(d+.d+),")                  // hdop
            .number("(d+),")                     // satellites
            .number("(d+),")                     // visible satellites
            .number("([^,]*),")                  // operator
            .number("(d+),")                     // rssi
            .number("[^,]*,")                    // cid
            .expression("([01]),")               // ignition
            .number("(d+.?d*),")                 // battery
            .number("(d+.?d*),")                 // power
            .expression("([01]+),")              // input
            .expression("([01]+),")              // output
            .expression("[01]+,")                // temper status
            .number("(d+.?d*)[^,]*,")            // adc1
            .number("(d+.?d*)[^,]*,")            // adc2
            .number("d+.?d*,")                   // trip meter
            .expression("([^,]*),")              // software version
            .expression("([^,]*),").optional()   // rfid
            .or()
            .number("[^,]*,")                    // cid
            .number("(d+),")                     // rssi
            .number("(d+),")                     // satellites
            .number("[^,]*,")                    // battery level
            .expression("([01]),")               // ignition
            .expression("([LH]{4}),")            // input
            .expression("[NT]{4},")              // tamper status
            .expression("([LH]{2}),")            // output
            .number("(d+.d+),")                  // adc1
            .number("(d+.d+),")                  // adc1
            .number("[^,]*,")                    // device id
            .number("(d+),")                     // odometer
            .groupEnd()
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String type = parser.next();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        switch (type) {
            case "rmv":
                position.set(Position.KEY_ALARM, Position.ALARM_POWER_CUT);
                break;
            case "ebl":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_POWER);
                break;
            case "ibl":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
                break;
            case "tmp":
            case "smt":
            case "btt":
                position.set(Position.KEY_ALARM, Position.ALARM_TAMPERING);
                break;
            case "ion":
                position.set(Position.KEY_IGNITION, true);
                break;
            case "iof":
                position.set(Position.KEY_IGNITION, false);
                break;
            default:
                break;
        }

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setValid(parser.nextInt() > 0);
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        if (parser.hasNext(15)) {

            position.setAltitude(parser.nextDouble());

            position.set(Position.KEY_HDOP, parser.nextDouble());
            position.set(Position.KEY_SATELLITES, parser.nextInt());
            position.set(Position.KEY_SATELLITES_VISIBLE, parser.nextInt());
            position.set(Position.KEY_OPERATOR, parser.next());
            position.set(Position.KEY_RSSI, parser.nextInt());
            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
            position.set(Position.KEY_BATTERY, parser.nextDouble());
            position.set(Position.KEY_POWER, parser.nextDouble());

            int input = parser.nextBinInt();
            int output = parser.nextBinInt();

            if (!BitUtil.check(input, 0)) {
                position.set(Position.KEY_ALARM, Position.ALARM_SOS);
            }

            position.set(Position.KEY_INPUT, input);
            position.set(Position.KEY_OUTPUT, output);

            position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
            position.set(Position.PREFIX_ADC + 2, parser.nextDouble());
            position.set(Position.KEY_VERSION_FW, parser.next());
            position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());

        }

        if (parser.hasNext(6)) {

            position.set(Position.KEY_RSSI, parser.nextInt());
            position.set(Position.KEY_SATELLITES, parser.nextInt());
            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);

            char[] input = parser.next().toCharArray();
            for (int i = 0; i < input.length; i++) {
                position.set(Position.PREFIX_IN + (i + 1), input[i] == 'H');
            }

            char[] output = parser.next().toCharArray();
            for (int i = 0; i < output.length; i++) {
                position.set(Position.PREFIX_OUT + (i + 1), output[i] == 'H');
            }

            position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
            position.set(Position.PREFIX_ADC + 2, parser.nextDouble());
            position.set(Position.KEY_ODOMETER, parser.nextInt());

        }

        return position;
    }

}
