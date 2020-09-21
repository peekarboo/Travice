
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

public class CguardProtocolDecoder extends BaseProtocolDecoder {

    public CguardProtocolDecoder(CguardProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_NV = new PatternBuilder()
            .text("NV:")
            .number("(dd)(dd)(dd) ")             // date (yymmdd)
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .number(":(-?d+.d+)")                // longitude
            .number(":(-?d+.d+)")                // latitude
            .number(":(d+.?d*)")                 // speed
            .number(":(?:NAN|(d+.?d*))")         // accuracy
            .number(":(?:NAN|(d+.?d*))")         // course
            .number(":(?:NAN|(d+.?d*))").optional() // altitude
            .compile();

    private static final Pattern PATTERN_BC = new PatternBuilder()
            .text("BC:")
            .number("(dd)(dd)(dd) ")             // date (yymmdd)
            .number("(dd)(dd)(dd):")             // time (hhmmss)
            .expression("(.+)")                  // data
            .compile();

    private Position decodePosition(DeviceSession deviceSession, String sentence) {

        Parser parser = new Parser(PATTERN_NV, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime());

        position.setValid(true);
        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));

        position.setAccuracy(parser.nextDouble(0));

        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        return position;
    }

    private Position decodeStatus(DeviceSession deviceSession, String sentence) {

        Parser parser = new Parser(PATTERN_BC, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        getLastLocation(position, parser.nextDateTime());

        String[] data = parser.next().split(":");
        for (int i = 0; i < data.length / 2; i++) {
            String key = data[i * 2];
            String value = data[i * 2 + 1];
            switch (key) {
                case "CSQ1":
                    position.set(Position.KEY_RSSI, Integer.parseInt(value));
                    break;
                case "NSQ1":
                    position.set(Position.KEY_SATELLITES, Integer.parseInt(value));
                    break;
                case "BAT1":
                    position.set(Position.KEY_BATTERY_LEVEL, Integer.parseInt(value));
                    break;
                case "PWR1":
                    position.set(Position.KEY_POWER, Double.parseDouble(value));
                    break;
                default:
                    position.set(key.toLowerCase(), value);
                    break;
            }
        }

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        if (sentence.startsWith("ID:") || sentence.startsWith("IDRO:")) {
            getDeviceSession(channel, remoteAddress, sentence.substring(sentence.indexOf(':') + 1));
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) {
            return null;
        }

        if (sentence.startsWith("NV:")) {
            return decodePosition(deviceSession, sentence);
        } else if (sentence.startsWith("BC:")) {
            return decodeStatus(deviceSession, sentence);
        }

        return null;
    }

}
