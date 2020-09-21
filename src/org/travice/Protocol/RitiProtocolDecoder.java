
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class RitiProtocolDecoder extends BaseProtocolDecoder {

    public RitiProtocolDecoder(RitiProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$GPRMC,")
            .number("(dd)(dd)(dd).?d*,")         // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*)?,")                // speed
            .number("(d+.?d*)?,")                // course
            .number("(dd)(dd)(dd)")              // date (ddmmyy)
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(2); // header

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, String.valueOf(buf.readUnsignedShort()));
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.set("mode", buf.readUnsignedByte());
        position.set(Position.KEY_COMMAND, buf.readUnsignedByte());
        position.set(Position.KEY_POWER, buf.readUnsignedShortLE() * 0.001);

        buf.skipBytes(5);  // status
        buf.readUnsignedShortLE();  // idleCount
        buf.readUnsignedShortLE();  // idleTime in seconds

        position.set(Position.KEY_DISTANCE, buf.readUnsignedIntLE());
        position.set(Position.KEY_ODOMETER_TRIP, buf.readUnsignedIntLE());

        // Parse GPRMC
        int end = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '*');
        String gprmc = buf.toString(buf.readerIndex(), end - buf.readerIndex(), StandardCharsets.US_ASCII);
        Parser parser = new Parser(PATTERN, gprmc);
        if (!parser.matches()) {
            return null;
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

        return position;
    }

}
