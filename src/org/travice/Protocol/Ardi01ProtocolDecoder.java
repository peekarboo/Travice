
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

public class Ardi01ProtocolDecoder extends BaseProtocolDecoder {

    public Ardi01ProtocolDecoder(Ardi01Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+),")                     // imei
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+.d+),")                // latitude
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*),")                 // course
            .number("(-?d+.?d*),")               // altitude
            .number("(d+),")                     // satellites
            .number("(d+),")                     // event
            .number("(d+),")                     // battery
            .number("(-?d+)")                    // temperature
            .compile();

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

        position.setTime(parser.nextDateTime());

        position.setLongitude(parser.nextDouble(0));
        position.setLatitude(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        int satellites = parser.nextInt(0);
        position.setValid(satellites >= 3);
        position.set(Position.KEY_SATELLITES, satellites);

        position.set(Position.KEY_EVENT, parser.next());
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt(0));
        position.set(Position.PREFIX_TEMP + 1, parser.next());

        return position;
    }

}
