
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

public class AuroProtocolDecoder extends BaseProtocolDecoder {

    public AuroProtocolDecoder(AuroProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("M(dddd)")                   // index
            .number("Td+")                       // phone
            .number("I(d+)")                     // imei
            .number("Ed+W")
            .text("*****")
            .number("d{8}d{4}")                  // local time
            .expression(".{8}#.{8}")
            .number("d{10}")                     // status
            .number("([-+])(ddd)(dd)(dddd)")     // longitude
            .number("([-+])(ddd)(dd)(dddd)")     // latitude
            .number("(dd)(dd)(dddd)")            // date (ddmmyyyy)
            .number("(dd)(dd)(dd)")              // time (hhmmss)
            .number("(ddd)")                     // course
            .number("d{6}")
            .number("(ddd)")                     // speed
            .number("d")
            .number("(dd)")                      // battery
            .expression("([01])")                // charging
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        position.set(Position.KEY_INDEX, parser.nextInt(0));

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setValid(true);
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setCourse(parser.nextDouble(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));

        position.set(Position.KEY_BATTERY, parser.nextInt(0));
        position.set(Position.KEY_CHARGE, parser.nextInt(0) == 1);

        return position;
    }

}
