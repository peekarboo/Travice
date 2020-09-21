
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class T57ProtocolDecoder extends BaseProtocolDecoder {

    public T57ProtocolDecoder(T57Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("*T57#")
            .number("Fd#")                       // type
            .number("([^#]+)#")                  // device id
            .number("(dd)(dd)(dd)#")             // date (ddmmyy)
            .number("(dd)(dd)(dd)#")             // time (hhmmss)
            .number("(dd)(dd.d+)#")              // latitude
            .expression("([NS])#")
            .number("(ddd)(dd.d+)#")             // longitude
            .expression("([EW])#")
            .expression("[^#]+#")
            .number("(d+.d+)#")                  // speed
            .number("(d+.d+)#")                  // altitude
            .expression("([AV])")                // valid
            .number("d#")                        // fix type
            .number("(d+.d+)#")                  // battery
            .compile();

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

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble());
        position.setAltitude(parser.nextDouble());

        position.setValid(parser.next().equals("A"));

        position.set(Position.KEY_BATTERY, parser.nextDouble());

        return position;
    }

}
