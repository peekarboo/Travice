
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Tlt2hProtocolDecoder extends BaseProtocolDecoder {

    public Tlt2hProtocolDecoder(Tlt2hProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_HEADER = new PatternBuilder()
            .number("#(d+)#")                    // imei
            .expression("[^#]*#")
            .number("d+#")
            .expression("([^#]+)#")              // status
            .number("d+")                        // number of records
            .compile();

    private static final Pattern PATTERN_POSITION = new PatternBuilder()
            .number("#(x+)?")                    // cell info
            .text("$GPRMC,")
            .number("(dd)(dd)(dd).d+,")          // time (hhmmss.sss)
            .expression("([AV]),")               // validity
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .number("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd)")              // date (ddmmyy)
            .any()
            .compile();

    private void decodeStatus(Position position, String status) {
        switch (status) {
            case "AUTOSTART":
            case "AUTO":
                position.set(Position.KEY_IGNITION, true);
                break;
            case "AUTOSTOP":
            case "AUTOLOW":
                position.set(Position.KEY_IGNITION, false);
                break;
            case "TOWED":
                position.set(Position.KEY_ALARM, Position.ALARM_TOW);
                break;
            case "SOS":
                position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                break;
            case "DEF":
                position.set(Position.KEY_ALARM, Position.ALARM_POWER_CUT);
                break;
            case "BLP":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
                break;
            case "CLP":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_POWER);
                break;
            case "OS":
                position.set(Position.KEY_ALARM, Position.ALARM_GEOFENCE_EXIT);
                break;
            case "RS":
                position.set(Position.KEY_ALARM, Position.ALARM_GEOFENCE_ENTER);
                break;
            case "OVERSPEED":
                position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
                break;
            default:
                break;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        sentence = sentence.trim();

        String header = sentence.substring(0, sentence.indexOf('\r'));
        Parser parser = new Parser(PATTERN_HEADER, header);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        String status = parser.next();

        String[] messages = sentence.substring(sentence.indexOf('\n') + 1).split("\r\n");
        List<Position> positions = new LinkedList<>();

        for (String message : messages) {
            parser = new Parser(PATTERN_POSITION, message);
            if (parser.matches()) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                parser.next(); // base station info

                DateBuilder dateBuilder = new DateBuilder()
                        .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

                position.setValid(parser.next().equals("A"));
                position.setLatitude(parser.nextCoordinate());
                position.setLongitude(parser.nextCoordinate());
                position.setSpeed(parser.nextDouble(0));
                position.setCourse(parser.nextDouble(0));

                dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
                position.setTime(dateBuilder.getDate());

                decodeStatus(position, status);

                positions.add(position);
            }
        }

        return positions;
    }

}
