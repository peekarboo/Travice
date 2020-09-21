
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class SanavProtocolDecoder extends BaseProtocolDecoder {

    public SanavProtocolDecoder(SanavProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("imei[:=]")
            .number("(d+)")                      // imei
            .expression("&?rmc[:=]")
            .text("$GPRMC,")
            .number("(dd)(dd)(dd).d+,")          // time (hhmmss.sss)
            .expression("([AV]),")               // validity
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([EW]),")
            .number("(d+.d+),")                  // speed
            .number("(d+.d+)?,")                 // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .groupBegin()
            .expression("[^*]*")
            .text("*")
            .number("xx,")
            .expression("[^,]+,")                // status
            .number("(d+),")                     // io
            .groupEnd("?")
            .any()
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

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble());
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        if (parser.hasNext()) {
            int io = parser.nextInt();
            for (int i = 0; i < 5; i++) {
                position.set(Position.PREFIX_IN + (i + 1), BitUtil.check(io, i));
            }
            position.set(Position.KEY_IGNITION, BitUtil.check(io, 5));
            position.set(Position.PREFIX_OUT + 1, BitUtil.check(io, 6));
            position.set(Position.PREFIX_OUT + 2, BitUtil.check(io, 7));
            position.set(Position.KEY_CHARGE, BitUtil.check(io, 8));
            if (!BitUtil.check(io, 9)) {
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
            }
        }

        return position;
    }

}
