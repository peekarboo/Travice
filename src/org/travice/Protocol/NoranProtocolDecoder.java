
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.BitUtil;
import org.travice.helper.DateBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class NoranProtocolDecoder extends BaseProtocolDecoder {

    public NoranProtocolDecoder(NoranProtocol protocol) {
        super(protocol);
    }

    public static final int MSG_UPLOAD_POSITION = 0x0008;
    public static final int MSG_UPLOAD_POSITION_NEW = 0x0032;
    public static final int MSG_CONTROL = 0x0002;
    public static final int MSG_CONTROL_RESPONSE = 0x8009;
    public static final int MSG_ALARM = 0x0003;
    public static final int MSG_SHAKE_HAND = 0x0000;
    public static final int MSG_SHAKE_HAND_RESPONSE = 0x8000;
    public static final int MSG_IMAGE_SIZE = 0x0200;
    public static final int MSG_IMAGE_PACKET = 0x0201;

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedShortLE(); // length
        int type = buf.readUnsignedShortLE();

        if (type == MSG_SHAKE_HAND && channel != null) {

            ByteBuf response = Unpooled.buffer(13);
            response.writeCharSequence("\r\n*KW", StandardCharsets.US_ASCII);
            response.writeByte(0);
            response.writeShortLE(response.capacity());
            response.writeShortLE(MSG_SHAKE_HAND_RESPONSE);
            response.writeByte(1); // status
            response.writeCharSequence("\r\n", StandardCharsets.US_ASCII);

            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));

        } else if (type == MSG_UPLOAD_POSITION || type == MSG_UPLOAD_POSITION_NEW
                || type == MSG_CONTROL_RESPONSE || type == MSG_ALARM) {

            boolean newFormat = false;
            if (type == MSG_UPLOAD_POSITION && buf.readableBytes() == 48
                    || type == MSG_ALARM && buf.readableBytes() == 48
                    || type == MSG_CONTROL_RESPONSE && buf.readableBytes() == 57) {
                newFormat = true;
            }

            Position position = new Position(getProtocolName());

            if (type == MSG_CONTROL_RESPONSE) {
                buf.readUnsignedIntLE(); // GIS ip
                buf.readUnsignedIntLE(); // GIS port
            }

            position.setValid(BitUtil.check(buf.readUnsignedByte(), 0));

            short alarm = buf.readUnsignedByte();
            switch (alarm) {
                case 1:
                    position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                    break;
                case 2:
                    position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
                    break;
                case 3:
                    position.set(Position.KEY_ALARM, Position.ALARM_GEOFENCE_EXIT);
                    break;
                case 9:
                    position.set(Position.KEY_ALARM, Position.ALARM_POWER_OFF);
                    break;
                default:
                    break;
            }

            if (newFormat) {
                position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedIntLE()));
                position.setCourse(buf.readFloatLE());
            } else {
                position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
                position.setCourse(buf.readUnsignedShortLE());
            }
            position.setLongitude(buf.readFloatLE());
            position.setLatitude(buf.readFloatLE());

            if (!newFormat) {
                long timeValue = buf.readUnsignedIntLE();
                DateBuilder dateBuilder = new DateBuilder()
                        .setYear((int) BitUtil.from(timeValue, 26))
                        .setMonth((int) BitUtil.between(timeValue, 22, 26))
                        .setDay((int) BitUtil.between(timeValue, 17, 22))
                        .setHour((int) BitUtil.between(timeValue, 12, 17))
                        .setMinute((int) BitUtil.between(timeValue, 6, 12))
                        .setSecond((int) BitUtil.to(timeValue, 6));
                position.setTime(dateBuilder.getDate());
            }

            ByteBuf rawId;
            if (newFormat) {
                rawId = buf.readSlice(12);
            } else {
                rawId = buf.readSlice(11);
            }
            String id = rawId.toString(StandardCharsets.US_ASCII).replaceAll("[^\\p{Print}]", "");
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
            if (deviceSession == null) {
                return null;
            }
            position.setDeviceId(deviceSession.getDeviceId());

            if (newFormat) {
                DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                position.setTime(dateFormat.parse(buf.readSlice(17).toString(StandardCharsets.US_ASCII)));
                buf.readByte();
            }

            if (!newFormat) {
                position.set(Position.PREFIX_IO + 1, buf.readUnsignedByte());
                position.set(Position.KEY_FUEL_LEVEL, buf.readUnsignedByte());
            } else if (type == MSG_UPLOAD_POSITION_NEW) {
                position.set(Position.PREFIX_TEMP + 1, buf.readShortLE());
                position.set(Position.KEY_ODOMETER, buf.readFloatLE());
            }

            return position;
        }

        return null;
    }

}
