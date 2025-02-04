
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.Context;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.regex.Pattern;

public class T55ProtocolDecoder extends BaseProtocolDecoder {

    public T55ProtocolDecoder(T55Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_GPRMC = new PatternBuilder()
            .text("$GPRMC,")
            .number("(dd)(dd)(dd).?d*,")         // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d{2,3})(dd.d+),")          // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .expression("[^*]+")
            .text("*")
            .expression("[^,]+")
            .number(",(d+)")                     // satellites
            .number(",(d+)")                     // imei
            .expression(",([01])")               // ignition
            .number(",(d+)")                     // fuel
            .number(",(d+)").optional(7)         // battery
            .number("((?:,d+)+)?")               // parameters
            .any()
            .compile();

    private static final Pattern PATTERN_GPGGA = new PatternBuilder()
            .text("$GPGGA,")
            .number("(dd)(dd)(dd).?d*,")         // time (hhmmss)
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([EW]),")
            .any()
            .compile();

    private static final Pattern PATTERN_GPRMA = new PatternBuilder()
            .text("$GPRMA,")
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),,,")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .any()
            .compile();

    private static final Pattern PATTERN_TRCCR = new PatternBuilder()
            .text("$TRCCR,")
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd).?d*,")         // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+.d+),")                  // speed
            .number("(d+.d+),")                  // course
            .number("(-?d+.d+),")                // altitude
            .number("(d+.?d*),")                 // battery
            .any()
            .compile();

    private Position position = null;

    private Position decodeGprmc(
            DeviceSession deviceSession, String sentence, SocketAddress remoteAddress, Channel channel) {

        if (deviceSession != null && channel != null && !(channel instanceof DatagramChannel)
                && Context.getIdentityManager().lookupAttributeBoolean(
                        deviceSession.getDeviceId(), getProtocolName() + ".ack", false, true)) {
            channel.writeAndFlush(new NetworkMessage("OK1\r\n", remoteAddress));
        }

        Parser parser = new Parser(PATTERN_GPRMC, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        if (deviceSession != null) {
            position.setDeviceId(deviceSession.getDeviceId());
        }

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        if (parser.hasNext(5)) {
            position.set(Position.KEY_SATELLITES, parser.nextInt());

            deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
            if (deviceSession == null) {
                return null;
            }
            position.setDeviceId(deviceSession.getDeviceId());

            position.set(Position.KEY_IGNITION, parser.hasNext() && parser.next().equals("1"));
            position.set(Position.KEY_FUEL_LEVEL, parser.nextInt(0));
            position.set(Position.KEY_BATTERY, parser.nextInt());
        }

        if (parser.hasNext()) {
            String[] parameters = parser.next().split(",");
            for (int i = 1; i < parameters.length; i++) {
                position.set(Position.PREFIX_IO + i, parameters[i]);
            }
        }

        if (deviceSession != null) {
            return position;
        } else {
            this.position = position; // save position
            return null;
        }
    }

    private Position decodeGpgga(DeviceSession deviceSession, String sentence) {

        Parser parser = new Parser(PATTERN_GPGGA, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setCurrentDate()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        position.setValid(true);
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());

        return position;
    }

    private Position decodeGprma(DeviceSession deviceSession, String sentence) {

        Parser parser = new Parser(PATTERN_GPRMA, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(new Date());
        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        return position;
    }

    private Position decodeTrccr(DeviceSession deviceSession, String sentence) {

        Parser parser = new Parser(PATTERN_TRCCR, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        position.set(Position.KEY_BATTERY, parser.nextDouble(0));

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        DeviceSession deviceSession;

        if (!sentence.startsWith("$") && sentence.contains("$")) {
            int index = sentence.indexOf("$");
            String id = sentence.substring(0, index);
            if (id.endsWith(",")) {
                id = id.substring(0, id.length() - 1);
            } else if (id.endsWith("/")) {
                id = id.substring(id.indexOf('/') + 1, id.length() - 1);
            }
            deviceSession = getDeviceSession(channel, remoteAddress, id);
            sentence = sentence.substring(index);
        } else {
            deviceSession = getDeviceSession(channel, remoteAddress);
        }

        if (sentence.startsWith("$PGID")) {
            getDeviceSession(channel, remoteAddress, sentence.substring(6, sentence.length() - 3));
        } else if (sentence.startsWith("$PCPTI")) {
            getDeviceSession(channel, remoteAddress, sentence.substring(7, sentence.indexOf(",", 7)));
        } else if (sentence.startsWith("IMEI")) {
            getDeviceSession(channel, remoteAddress, sentence.substring(5));
        } else if (sentence.startsWith("$IMEI")) {
            getDeviceSession(channel, remoteAddress, sentence.substring(6));
        } else if (sentence.startsWith("$GPFID")) {
            deviceSession = getDeviceSession(channel, remoteAddress, sentence.substring(7));
            if (deviceSession != null && position != null) {
                Position position = this.position;
                position.setDeviceId(deviceSession.getDeviceId());
                this.position = null;
                return position;
            }
        } else if (sentence.matches("^[0-9A-F]+$")) {
            getDeviceSession(channel, remoteAddress, sentence);
        } else if (sentence.startsWith("$GPRMC")) {
            return decodeGprmc(deviceSession, sentence, remoteAddress, channel);
        } else if (sentence.startsWith("$GPGGA") && deviceSession != null) {
            return decodeGpgga(deviceSession, sentence);
        } else if (sentence.startsWith("$GPRMA") && deviceSession != null) {
            return decodeGprma(deviceSession, sentence);
        } else if (sentence.startsWith("$TRCCR") && deviceSession != null) {
            return decodeTrccr(deviceSession, sentence);
        }

        return null;
    }

}
