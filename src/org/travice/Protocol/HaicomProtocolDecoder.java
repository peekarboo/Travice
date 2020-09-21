
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class HaicomProtocolDecoder extends BaseProtocolDecoder {

    public HaicomProtocolDecoder(HaicomProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$GPRS")
            .number("(d+),")                     // imei
            .expression("([^,]+),")              // version
            .number("(dd)(dd)(dd),")             // date (yymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(d)")                       // flags
            .number("(dd)(d{5})")                // latitude
            .number("(ddd)(d{5}),")              // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // status
            .number("(d+)?,")                    // gprs counting value
            .number("(d+)?,")                    // gps power saving counting value
            .number("(d+),")                     // switch status
            .number("(d+)")                      // relay status
            .expression("(?:[LH]{2})?")          // power status
            .number("#V(d+)")                    // battery
            .any()
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

        position.set(Position.KEY_VERSION_FW, parser.next());

        position.setTime(parser.nextDateTime());

        int flags = parser.nextInt(0);

        position.setValid(BitUtil.check(flags, 0));

        double latitude = parser.nextDouble(0) + parser.nextDouble(0) / 60000;
        if (BitUtil.check(flags, 2)) {
            position.setLatitude(latitude);
        } else {
            position.setLatitude(-latitude);
        }

        double longitude = parser.nextDouble(0) + parser.nextDouble(0) / 60000;
        if (BitUtil.check(flags, 1)) {
            position.setLongitude(longitude);
        } else {
            position.setLongitude(-longitude);
        }

        position.setSpeed(parser.nextDouble(0) / 10);
        position.setCourse(parser.nextDouble(0) / 10);

        position.set(Position.KEY_STATUS, parser.next());
        position.set("gprsCount", parser.next());
        position.set("powersaveCountdown", parser.next());
        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_OUTPUT, parser.next());
        position.set(Position.KEY_BATTERY, parser.nextDouble(0) * 0.1);

        return position;
    }

}
