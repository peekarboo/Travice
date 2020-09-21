
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;

public class GpsmtaProtocolDecoder extends BaseProtocolDecoder {

    public GpsmtaProtocolDecoder(GpsmtaProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .expression("([^ ]+) ")              // uid
            .number("(d+) ")                     // time (unix time)
            .number("(d+.d+) ")                  // latitude
            .number("(d+.d+) ")                  // longitude
            .number("(d+) ")                     // speed
            .number("(d+) ")                     // course
            .number("(d+) ")                     // accuracy
            .number("(d+) ")                     // altitude
            .number("(d+) ")                     // flags
            .number("(d+) ")                     // battery
            .number("(d+) ")                     // temperature
            .number("(d)")                       // changing status
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

        String time = parser.next();
        position.setTime(new Date(Long.parseLong(time) * 1000));

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setSpeed(parser.nextInt(0));
        position.setCourse(parser.nextInt(0));
        position.setAccuracy(parser.nextInt(0));
        position.setAltitude(parser.nextInt(0));

        position.set(Position.KEY_STATUS, parser.nextInt(0));
        position.set(Position.KEY_BATTERY, parser.nextInt(0));
        position.set(Position.PREFIX_TEMP + 1, parser.nextInt(0));
        position.set(Position.KEY_CHARGE, parser.nextInt(0) == 1);

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(time, remoteAddress));
        }

        return position;
    }

}
