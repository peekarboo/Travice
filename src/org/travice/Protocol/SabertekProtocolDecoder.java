
package org.travice.protocol;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.BitUtil;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;

public class SabertekProtocolDecoder extends BaseProtocolDecoder {

    public SabertekProtocolDecoder(SabertekProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text(",")
            .number("(d+),")                     // id
            .number("d,")                        // type
            .groupBegin()
            .number("d+,")                       // imei
            .number("d+,")                       // scid
            .expression("[^,]*,")                // phone
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .groupEnd("?")
            .number("(d+),")                     // battery
            .number("(d+),")                     // rssi
            .number("(d+),")                     // state
            .number("(d+),")                     // events
            .number("(d),")                      // valid
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(d+),")                     // altitude
            .number("(d+),")                     // satellites
            .number("(d+),")                     // odometer
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(
                    Unpooled.wrappedBuffer(new byte[]{(byte) (deviceSession != null ? 0x06 : 0x15)}), remoteAddress));
        }
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        if (parser.hasNext(6)) {
            position.setTime(parser.nextDateTime());
        } else {
            position.setTime(new Date());
        }

        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
        position.set(Position.KEY_RSSI, parser.nextInt());

        int state = parser.nextInt();

        position.set(Position.KEY_IGNITION, BitUtil.check(state, 0));
        position.set(Position.KEY_CHARGE, BitUtil.check(state, 1));

        if (BitUtil.check(state, 2)) {
            position.set(Position.KEY_ALARM, Position.ALARM_JAMMING);
        }
        if (BitUtil.check(state, 3)) {
            position.set(Position.KEY_ALARM, Position.ALARM_TAMPERING);
        }

        int events = parser.nextInt();

        if (BitUtil.check(events, 0)) {
            position.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
        }
        if (BitUtil.check(events, 1)) {
            position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
        }
        if (BitUtil.check(events, 2)) {
            position.set(Position.KEY_ALARM, Position.ALARM_ACCIDENT);
        }
        if (BitUtil.check(events, 3)) {
            position.set(Position.KEY_ALARM, Position.ALARM_CORNERING);
        }

        position.setValid(parser.nextInt() == 1);
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt()));
        position.setCourse(parser.nextInt());
        position.setAltitude(parser.nextInt());

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_ODOMETER, parser.nextInt() * 1000L);

        return position;
    }

}
