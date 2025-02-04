
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.Context;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class StarLinkProtocolDecoder extends BaseProtocolDecoder {

    private String[] dataTags;
    private DateFormat dateFormat;

    public StarLinkProtocolDecoder(StarLinkProtocol protocol) {
        super(protocol);

        String format = Context.getConfig().getString(
                getProtocolName() + ".format", "#EDT#,#EID#,#PDT#,#LAT#,#LONG#,#SPD#,#HEAD#,#ODO#,"
                + "#IN1#,#IN2#,#IN3#,#IN4#,#OUT1#,#OUT2#,#OUT3#,#OUT4#,#LAC#,#CID#,#VIN#,#VBAT#,#DEST#,#IGN#,#ENG#");
        dataTags = format.split(",");

        dateFormat = new SimpleDateFormat(
                Context.getConfig().getString(getProtocolName() + ".dateFormat", "yyMMddHHmmss"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression(".")                     // protocol head
            .text("SLU")                         // message head
            .number("(x{6}|d{15}),")             // id
            .number("(d+),")                     // type
            .number("(d+),")                     // index
            .expression("(.+)")                  // data
            .text("*")
            .number("xx")                        // checksum
            .compile();

    public static final int MSG_EVENT_REPORT = 6;

    private double parseCoordinate(String value) {
        int minutesIndex = value.indexOf('.') - 2;
        double result = Double.parseDouble(value.substring(1, minutesIndex));
        result += Double.parseDouble(value.substring(minutesIndex)) / 60;
        return value.charAt(0) == '+' ? result : -result;
    }

    private String decodeAlarm(int event) {
        switch (event) {
            case 6:
                return Position.ALARM_OVERSPEED;
            case 7:
                return Position.ALARM_GEOFENCE_ENTER;
            case 8:
                return Position.ALARM_GEOFENCE_EXIT;
            case 9:
                return Position.ALARM_POWER_CUT;
            case 11:
                return Position.ALARM_LOW_BATTERY;
            case 26:
                return Position.ALARM_TOW;
            case 36:
                return Position.ALARM_SOS;
            case 42:
                return Position.ALARM_JAMMING;
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

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        int type = parser.nextInt(0);
        if (type != MSG_EVENT_REPORT) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setValid(true);

        position.set(Position.KEY_INDEX, parser.nextInt(0));

        String[] data = parser.next().split(",");
        Integer lac = null, cid = null;
        int event = 0;

        for (int i = 0; i < Math.min(data.length, dataTags.length); i++) {
            if (data[i].isEmpty()) {
                continue;
            }
            switch (dataTags[i]) {
                case "#EDT#":
                    position.setDeviceTime(dateFormat.parse(data[i]));
                    break;
                case "#EID#":
                    event = Integer.parseInt(data[i]);
                    position.set(Position.KEY_ALARM, decodeAlarm(event));
                    position.set(Position.KEY_EVENT, event);
                    break;
                case "#PDT#":
                    position.setFixTime(dateFormat.parse(data[i]));
                    break;
                case "#LAT#":
                    position.setLatitude(parseCoordinate(data[i]));
                    break;
                case "#LONG#":
                    position.setLongitude(parseCoordinate(data[i]));
                    break;
                case "#SPD#":
                    position.setSpeed(Double.parseDouble(data[i]));
                    break;
                case "#HEAD#":
                    position.setCourse(Integer.parseInt(data[i]));
                    break;
                case "#ODO#":
                    position.set(Position.KEY_ODOMETER, Long.parseLong(data[i]) * 1000);
                    break;
                case "#IN1#":
                    position.set(Position.PREFIX_IN + 1, Integer.parseInt(data[i]));
                    break;
                case "#IN2#":
                    position.set(Position.PREFIX_IN + 2, Integer.parseInt(data[i]));
                    break;
                case "#IN3#":
                    position.set(Position.PREFIX_IN + 3, Integer.parseInt(data[i]));
                    break;
                case "#IN4#":
                    position.set(Position.PREFIX_IN + 4, Integer.parseInt(data[i]));
                    break;
                case "#OUT1#":
                    position.set(Position.PREFIX_OUT + 1, Integer.parseInt(data[i]));
                    break;
                case "#OUT2#":
                    position.set(Position.PREFIX_OUT + 2, Integer.parseInt(data[i]));
                    break;
                case "#OUT3#":
                    position.set(Position.PREFIX_OUT + 3, Integer.parseInt(data[i]));
                    break;
                case "#OUT4#":
                    position.set(Position.PREFIX_OUT + 4, Integer.parseInt(data[i]));
                    break;
                case "#LAC#":
                    if (!data[i].isEmpty()) {
                        lac = Integer.parseInt(data[i]);
                    }
                    break;
                case "#CID#":
                    if (!data[i].isEmpty()) {
                        cid = Integer.parseInt(data[i]);
                    }
                    break;
                case "#VIN#":
                    position.set(Position.KEY_POWER, Double.parseDouble(data[i]));
                    break;
                case "#VBAT#":
                    position.set(Position.KEY_BATTERY, Double.parseDouble(data[i]));
                    break;
                case "#DEST#":
                    position.set("destination", data[i]);
                    break;
                case "#IGN#":
                    position.set(Position.KEY_IGNITION, data[i].equals("1"));
                    break;
                case "#ENG#":
                    position.set("engine", data[i].equals("1"));
                    break;
                default:
                    break;
            }
        }

        if (position.getFixTime() == null) {
            getLastLocation(position, null);
        }

        if (lac != null && cid != null) {
            position.setNetwork(new Network(CellTower.fromLacCid(lac, cid)));
        }

        if (event == 20) {
            String rfid = data[data.length - 1];
            if (rfid.matches("0+")) {
                rfid = data[data.length - 2];
            }
            position.set(Position.KEY_DRIVER_UNIQUE_ID, rfid);
        }

        return position;
    }

}
