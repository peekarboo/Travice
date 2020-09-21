
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Checksum;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class L100ProtocolDecoder extends BaseProtocolDecoder {

    public L100ProtocolDecoder(L100Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("ATL")
            .number("(d{15}),")                  // imei
            .text("$GPRMC,")
            .number("(dd)(dd)(dd)")              // time (hhmmss.sss)
            .number(".(ddd)").optional()
            .expression(",([AV]),")              // validity
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .any()
            .text("#")
            .number("([01]+),")                  // io status
            .number("(d+.?d*|N.C),")             // adc
            .expression("[^,]*,")                // reserved
            .expression("[^,]*,")                // reserved
            .number("(d+.?d*),")                 // odometer
            .number("(d+.?d*),")                 // temperature
            .number("(d+.?d*),")                 // battery
            .number("(d+),")                     // rssi
            .number("(d+),")                     // mcc
            .number("(d+),")                     // mnc
            .number("(x+),")                     // lac
            .number("(x+)")                      // cid
            .any()
            .text("ATL")
            .compile();

    private static final Pattern PATTERN_OBD = new PatternBuilder()
            .expression("[LH],")                 // archive
            .text("ATL,")
            .number("(d{15}),")                  // imei
            .number("(d+),")                     // type
            .number("(d+),")                     // index
            .groupBegin()
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .expression("([AV]),")               // validity
            .number("(d+.d+);([NS]),")           // latitude
            .number("(d+.d+);([EW]),")           // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+.d+),")                  // odometer
            .number("(d+.d+),")                  // battery
            .number("(d+),")                     // rssi
            .number("(d+),")                     // mcc
            .number("(d+),")                     // mnc
            .number("(d+),")                     // lac
            .number("(x+),")                     // cid
            .number("#(d)(d)(d)(d),")            // status
            .number("(d),")                      // overspeed
            .text("ATL,")
            .groupEnd("?")
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        if (sentence.startsWith("L") || sentence.startsWith("H")) {
            return decodeObd(channel, remoteAddress, sentence);
        } else {
            return decodeNormal(channel, remoteAddress, sentence);
        }
    }

    private Object decodeNormal(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt(), parser.nextInt(0));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_STATUS, parser.next());
        position.set(Position.PREFIX_ADC + 1, parser.next());
        position.set(Position.KEY_ODOMETER, parser.nextDouble());
        position.set(Position.PREFIX_TEMP + 1, parser.nextDouble());
        position.set(Position.KEY_BATTERY, parser.nextDouble());

        int rssi = parser.nextInt();
        if (rssi > 0) {
            position.setNetwork(new Network(CellTower.from(
                    parser.nextInt(), parser.nextInt(), parser.nextHexInt(), parser.nextHexInt(), rssi)));
        }

        return position;
    }

    private Object decodeObd(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN_OBD, sentence);
        if (!parser.matches()) {
            return null;
        }

        String imei = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        int type = parser.nextInt();
        int index = parser.nextInt();

        if (type == 1) {
            if (channel != null) {
                String response = "@" + imei + ",00," + index + ",";
                response += "*" + (char) Checksum.xor(response);
                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setSpeed(parser.nextInt());
        position.setCourse(parser.nextInt());

        position.set(Position.KEY_ODOMETER, parser.nextDouble() * 1000);
        position.set(Position.KEY_BATTERY, parser.nextDouble());

        int rssi = parser.nextInt();
        position.setNetwork(new Network(CellTower.from(
                parser.nextInt(), parser.nextInt(), parser.nextInt(), parser.nextHexInt(), rssi)));

        position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
        parser.next(); // reserved

        switch (parser.nextInt()) {
            case 0:
                position.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
                break;
            case 2:
                position.set(Position.KEY_ALARM, Position.ALARM_ACCELERATION);
                break;
            case 1:
                position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
                break;
            default:
                break;
        }

        position.set(Position.KEY_CHARGE, parser.nextInt() == 1);

        if (parser.nextInt() == 1) {
            position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
        }

        return position;
    }

}
