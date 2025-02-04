
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class TrakMateProtocolDecoder extends BaseProtocolDecoder {

    public TrakMateProtocolDecoder(TrakMateProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_SRT = new PatternBuilder()
            .text("^TMSRT|")
            .expression("([^ ]+)|")              // uid
            .number("(d+.d+)|")                  // latitude
            .number("(d+.d+)|")                  // longitude
            .number("(dd)(dd)(dd)|")             // time (hhmmss)
            .number("(dd)(dd)(dd)|")             // date (ddmmyy)
            .number("(d+.d+)|")                  // software ver
            .number("(d+.d+)|")                  // Hardware ver
            .any()
            .compile();

    private static final Pattern PATTERN_PER = new PatternBuilder()
            .text("^TMPER|")
            .expression("([^ ]+)|")              // uid
            .number("(d+)|")                     // seq
            .number("(d+.d+)|")                  // latitude
            .number("(d+.d+)|")                  // longitude
            .number("(dd)(dd)(dd)|")             // time (hhmmss)
            .number("(dd)(dd)(dd)|")             // date (ddmmyy)
            .number("(d+.d+)|")                  // speed
            .number("(d+.d+)|")                  // heading
            .number("(d+)|")                     // ignition
            .number("(d+)|")                     // dop1
            .number("(d+)|")                     // dop2
            .number("(d+.d+)|")                  // analog
            .number("(d+.d+)|")                  // internal battery
            .number("(d+.d+)|")                  // vehicle battery
            .number("(d+.d+)|")                  // gps odometer
            .number("(d+.d+)|")                  // pulse odometer
            .number("(d+)|")                     // main power status
            .number("(d+)|")                     // gps data validity
            .number("(d+)|")                     // live or cache
            .any()
            .compile();

    private static final Pattern PATTERN_ALT = new PatternBuilder()
            .text("^TMALT|")
            .expression("([^ ]+)|")              // uid
            .number("(d+)|")                     // seq
            .number("(d+)|")                     // Alert type
            .number("(d+)|")                     // Alert status
            .number("(d+.d+)|")                  // latitude
            .number("(d+.d+)|")                  // longitude
            .number("(dd)(dd)(dd)|")             // time (hhmmss)
            .number("(dd)(dd)(dd)|")             // date (ddmmyy)
            .number("(d+.d+)|")                  // speed
            .number("(d+.d+)|")                  // heading
            .any()
            .compile();

    private String decodeAlarm(int value) {
        switch (value) {
            case 1:
                return Position.ALARM_SOS;
            case 3:
                return Position.ALARM_GEOFENCE;
            case 4:
                return Position.ALARM_POWER_CUT;
            default:
                return null;
        }
    }

    private Object decodeSrt(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN_SRT, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));

        position.set(Position.KEY_VERSION_FW, parser.next());
        position.set(Position.KEY_VERSION_HW, parser.next());

        return position;
    }

    private Object decodeAlt(Channel channel, SocketAddress remoteAddress, String sentence) {

        Parser parser = new Parser(PATTERN_ALT, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        parser.next(); // seq
        position.set(Position.KEY_ALARM, decodeAlarm(parser.nextInt(0)));
        parser.next(); // alert status or data

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));

        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        return position;
    }

    private Object decodePer(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN_PER, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        parser.next(); // seq

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));

        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_IGNITION, parser.nextInt(0) == 1);
        position.set("dop1", parser.next());
        position.set("dop2", parser.next());
        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_BATTERY, parser.nextDouble(0));
        position.set(Position.KEY_POWER, parser.nextDouble());
        position.set(Position.KEY_ODOMETER, parser.nextDouble(0));
        position.set("pulseOdometer", parser.next());
        position.set(Position.KEY_STATUS, parser.nextInt(0));

        position.setValid(parser.nextInt(0) != 0);

        position.set(Position.KEY_ARCHIVE, parser.nextInt(0) == 1);

        return position;
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        int typeIndex = sentence.indexOf("^TM");
        if (typeIndex < 0) {
            return null;
        }

        String type = sentence.substring(typeIndex + 3, typeIndex + 6);
        switch (type) {
            case "ALT":
                return decodeAlt(channel, remoteAddress, sentence);
            case "SRT":
                return decodeSrt(channel, remoteAddress, sentence);
            case "PER":
                return decodePer(channel, remoteAddress, sentence);
            default:
                return null;
        }
    }

}
