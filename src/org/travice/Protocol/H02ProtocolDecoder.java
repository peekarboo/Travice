
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.BcdUtil;
import org.travice.helper.BitUtil;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class H02ProtocolDecoder extends BaseProtocolDecoder {

    public H02ProtocolDecoder(H02Protocol protocol) {
        super(protocol);
    }

    private static double readCoordinate(ByteBuf buf, boolean lon) {

        int degrees = BcdUtil.readInteger(buf, 2);
        if (lon) {
            degrees = degrees * 10 + (buf.getUnsignedByte(buf.readerIndex()) >> 4);
        }

        double result = 0;
        if (lon) {
            result = buf.readUnsignedByte() & 0x0f;
        }

        int length = 6;
        if (lon) {
            length = 5;
        }

        result = result * 10 + BcdUtil.readInteger(buf, length) * 0.0001;

        result /= 60;
        result += degrees;

        return result;
    }

    private void processStatus(Position position, long status) {

        if (!BitUtil.check(status, 0)) {
            position.set(Position.KEY_ALARM, Position.ALARM_VIBRATION);
        } else if (!BitUtil.check(status, 1)) {
            position.set(Position.KEY_ALARM, Position.ALARM_SOS);
        } else if (!BitUtil.check(status, 2)) {
            position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
        } else if (!BitUtil.check(status, 19)) {
            position.set(Position.KEY_ALARM, Position.ALARM_POWER_CUT);
        }

        position.set(Position.KEY_IGNITION, BitUtil.check(status, 10));
        position.set(Position.KEY_STATUS, status);

    }

    private Integer decodeBattery(int value) {
        switch (value) {
            case 6:
                return 100;
            case 5:
                return 80;
            case 4:
                return 60;
            case 3:
                return 20;
            case 2:
                return 10;
            default:
                return null;
        }
    }

    private Position decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {

        Position position = new Position(getProtocolName());

        buf.readByte(); // marker

        DeviceSession deviceSession = getDeviceSession(
                channel, remoteAddress, ByteBufUtil.hexDump(buf.readSlice(5)));
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setHour(BcdUtil.readInteger(buf, 2))
                .setMinute(BcdUtil.readInteger(buf, 2))
                .setSecond(BcdUtil.readInteger(buf, 2))
                .setDay(BcdUtil.readInteger(buf, 2))
                .setMonth(BcdUtil.readInteger(buf, 2))
                .setYear(BcdUtil.readInteger(buf, 2));
        position.setTime(dateBuilder.getDate());

        double latitude = readCoordinate(buf, false);
        position.set(Position.KEY_BATTERY_LEVEL, decodeBattery(buf.readUnsignedByte()));
        double longitude = readCoordinate(buf, true);

        int flags = buf.readUnsignedByte() & 0x0f;
        position.setValid((flags & 0x02) != 0);
        if ((flags & 0x04) == 0) {
            latitude = -latitude;
        }
        if ((flags & 0x08) == 0) {
            longitude = -longitude;
        }

        position.setLatitude(latitude);
        position.setLongitude(longitude);

        position.setSpeed(BcdUtil.readInteger(buf, 3));
        position.setCourse((buf.readUnsignedByte() & 0x0f) * 100.0 + BcdUtil.readInteger(buf, 2));

        processStatus(position, buf.readUnsignedInt());

        return position;
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("*")
            .expression("..,")                   // manufacturer
            .number("(d+)?,")                    // imei
            .groupBegin()
            .text("VP1,")
            .or()
            .groupBegin()
            .text("V4,")
            .expression("(.*),")                 // response
            .or()
            .expression("(V[^,]*),")
            .groupEnd()
            .number("(?:(dd)(dd)(dd))?,")        // time (hhmmss)
            .groupEnd()
            .groupBegin()
            .expression("([ABV])?,")             // validity
            .or()
            .number("(d+),")                     // coding scheme
            .groupEnd()
            .groupBegin()
            .number("-(d+)-(d+.d+),")            // latitude
            .or()
            .number("(d+)(dd.d+),")              // latitude
            .groupEnd()
            .expression("([NS]),")
            .groupBegin()
            .number("-(d+)-(d+.d+),")            // longitude
            .or()
            .number("(d+)(dd.d+),")              // longitude
            .groupEnd()
            .expression("([EW]),")
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*)?,")                // course
            .number("(?:(dd)(dd)(dd))?")         // date (ddmmyy)
            .groupBegin()
            .expression(",[^,]*,")
            .expression("[^,]*,")
            .expression("[^,]*")                 // sim info
            .groupEnd("?")
            .groupBegin()
            .number(",(x{8})")
            .groupBegin()
            .number(",(d+),")                    // odometer
            .number("(-?d+),")                   // temperature
            .number("(d+.d+),")                  // fuel
            .number("(-?d+),")                   // altitude
            .number("(x+),")                     // lac
            .number("(x+)")                      // cid
            .or()
            .text(",")
            .expression("(.*)")                  // data
            .or()
            .groupEnd()
            .or()
            .groupEnd()
            .text("#")
            .compile();

    private static final Pattern PATTERN_NBR = new PatternBuilder()
            .text("*")
            .expression("..,")                   // manufacturer
            .number("(d+),")                     // imei
            .text("NBR,")
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d+),")                     // mcc
            .number("(d+),")                     // mnc
            .number("d+,")                       // gsm delay time
            .number("d+,")                       // count
            .number("((?:d+,d+,d+,)+)")          // cells
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(x{8})")                    // status
            .any()
            .compile();

    private static final Pattern PATTERN_LINK = new PatternBuilder()
            .text("*")
            .expression("..,")                   // manufacturer
            .number("(d+),")                     // imei
            .text("LINK,")
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d+),")                     // rssi
            .number("(d+),")                     // satellites
            .number("(d+),")                     // battery
            .number("(d+),")                     // steps
            .number("(d+),")                     // turnovers
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(x{8})")                    // status
            .any()
            .compile();

    private static final Pattern PATTERN_V3 = new PatternBuilder()
            .text("*")
            .expression("..,")                   // manufacturer
            .number("(d+),")                     // imei
            .text("V3,")
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(ddd)")                     // mcc
            .number("(d+),")                     // mnc
            .number("(d+),")                     // count
            .expression("(.*),")                 // cell info
            .number("(x{4}),")                   // battery
            .number("d+,")                       // reboot info
            .text("X,")
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(x{8})")                    // status
            .text("#").optional()
            .compile();

    private void sendResponse(Channel channel, SocketAddress remoteAddress, String id, String type) {
        if (channel != null && id != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String response = String.format("*HQ,%s,V4,%s,%s#", id, type, dateFormat.format(new Date()));
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    private Position decodeText(String sentence, Channel channel, SocketAddress remoteAddress) {

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        String id = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        if (parser.hasNext()) {
            position.set(Position.KEY_RESULT, parser.next());
        }

        if (parser.hasNext() && parser.next().equals("V1")) {
            sendResponse(channel, remoteAddress, id, "V1");
        }

        DateBuilder dateBuilder = new DateBuilder();
        if (parser.hasNext(3)) {
            dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        }

        if (parser.hasNext()) {
            position.setValid(parser.next().equals("A"));
        }
        if (parser.hasNext()) {
            parser.nextInt(); // coding scheme
            position.setValid(true);
        }

        if (parser.hasNext(2)) {
            position.setLatitude(-parser.nextCoordinate());
        }
        if (parser.hasNext(2)) {
            position.setLatitude(parser.nextCoordinate());
        }

        if (parser.hasNext(2)) {
            position.setLongitude(-parser.nextCoordinate());
        }
        if (parser.hasNext(2)) {
            position.setLongitude(parser.nextCoordinate());
        }

        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        if (parser.hasNext(3)) {
            dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
            position.setTime(dateBuilder.getDate());
        } else {
            position.setTime(new Date());
        }

        if (parser.hasNext()) {
            processStatus(position, parser.nextLong(16, 0));
        }

        if (parser.hasNext(6)) {
            position.set(Position.KEY_ODOMETER, parser.nextInt(0));
            position.set(Position.PREFIX_TEMP + 1, parser.nextInt(0));
            position.set(Position.KEY_FUEL_LEVEL, parser.nextDouble(0));

            position.setAltitude(parser.nextInt(0));

            position.setNetwork(new Network(CellTower.fromLacCid(parser.nextHexInt(0), parser.nextHexInt(0))));
        }

        if (parser.hasNext(4)) {
            String[] values = parser.next().split(",");
            for (int i = 0; i < values.length; i++) {
                position.set(Position.PREFIX_IO + (i + 1), values[i].trim());
            }
        }

        return position;
    }

    private Position decodeLbs(String sentence, Channel channel, SocketAddress remoteAddress) {

        Parser parser = new Parser(PATTERN_NBR, sentence);
        if (!parser.matches()) {
            return null;
        }

        String id = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        sendResponse(channel, remoteAddress, id, "NBR");

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        Network network = new Network();
        int mcc = parser.nextInt(0);
        int mnc = parser.nextInt(0);

        String[] cells = parser.next().split(",");
        for (int i = 0; i < cells.length / 3; i++) {
            network.addCellTower(CellTower.from(mcc, mnc, Integer.parseInt(cells[i * 3]),
                    Integer.parseInt(cells[i * 3 + 1]), Integer.parseInt(cells[i * 3 + 2])));
        }

        position.setNetwork(network);

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        getLastLocation(position, dateBuilder.getDate());

        processStatus(position, parser.nextLong(16, 0));

        return position;
    }

    private Position decodeLink(String sentence, Channel channel, SocketAddress remoteAddress) {

        Parser parser = new Parser(PATTERN_LINK, sentence);
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
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.set(Position.KEY_RSSI, parser.nextInt());
        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
        position.set(Position.KEY_STEPS, parser.nextInt());
        position.set("turnovers", parser.nextInt());

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        getLastLocation(position, dateBuilder.getDate());

        processStatus(position, parser.nextLong(16, 0));

        return position;
    }

    private Position decodeV3(String sentence, Channel channel, SocketAddress remoteAddress) {

        Parser parser = new Parser(PATTERN_V3, sentence);
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
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        int mcc = parser.nextInt();
        int mnc = parser.nextInt();

        int count = parser.nextInt();
        Network network = new Network();
        String[] values = parser.next().split(",");
        for (int i = 0; i < count; i++) {
            network.addCellTower(CellTower.from(
                    mcc, mnc, Integer.parseInt(values[i * 4]), Integer.parseInt(values[i * 4 + 1])));
        }
        position.setNetwork(network);

        position.set(Position.KEY_BATTERY, parser.nextHexInt());

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        getLastLocation(position, dateBuilder.getDate());

        processStatus(position, parser.nextLong(16, 0));

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        String marker = buf.toString(0, 1, StandardCharsets.US_ASCII);

        switch (marker) {
            case "*":
                String sentence = buf.toString(StandardCharsets.US_ASCII).trim();
                int typeStart = sentence.indexOf(',', sentence.indexOf(',') + 1) + 1;
                int typeEnd = sentence.indexOf(',', typeStart);
                if (typeEnd > 0) {
                    String type = sentence.substring(typeStart, typeEnd);
                    switch (type) {
                        case "NBR":
                            return decodeLbs(sentence, channel, remoteAddress);
                        case "LINK":
                            return decodeLink(sentence, channel, remoteAddress);
                        case "V3":
                            return decodeV3(sentence, channel, remoteAddress);
                        default:
                            return decodeText(sentence, channel, remoteAddress);
                    }
                } else {
                    return null;
                }
            case "$":
                return decodeBinary(buf, channel, remoteAddress);
            case "X":
            default:
                return null;
        }
    }

}
