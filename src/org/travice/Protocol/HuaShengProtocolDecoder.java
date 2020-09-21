
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

public class HuaShengProtocolDecoder extends BaseProtocolDecoder {

    public HuaShengProtocolDecoder(HuaShengProtocol protocol) {
        super(protocol);
    }

    public static final int MSG_POSITION = 0xAA00;
    public static final int MSG_POSITION_RSP = 0xFF01;
    public static final int MSG_LOGIN = 0xAA02;
    public static final int MSG_LOGIN_RSP = 0xFF03;
    public static final int MSG_HSO_REQ = 0x0002;
    public static final int MSG_HSO_RSP = 0x0003;

    private void sendResponse(Channel channel, int type, int index, ByteBuf content) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeByte(0xC0);
            response.writeShort(0x0100);
            response.writeShort(12 + (content != null ? content.readableBytes() : 0));
            response.writeShort(type);
            response.writeShort(0);
            response.writeInt(index);
            if (content != null) {
                response.writeBytes(content);
                content.release();
            }
            response.writeByte(0xC0);
            channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(1); // start marker
        buf.readUnsignedByte(); // flag
        buf.readUnsignedByte(); // reserved
        buf.readUnsignedShort(); // length

        int type = buf.readUnsignedShort();

        buf.readUnsignedShort(); // checksum
        int index = buf.readInt();

        if (type == MSG_LOGIN) {

            while (buf.readableBytes() > 4) {
                int subtype = buf.readUnsignedShort();
                int length = buf.readUnsignedShort() - 4;
                if (subtype == 0x0003) {
                    String imei = buf.readSlice(length).toString(StandardCharsets.US_ASCII);
                    DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
                    if (deviceSession != null && channel != null) {
                        ByteBuf content = Unpooled.buffer();
                        content.writeByte(0); // success
                        sendResponse(channel, MSG_LOGIN_RSP, index, content);
                    }
                } else {
                    buf.skipBytes(length);
                }
            }

        } else if (type == MSG_HSO_REQ) {

            sendResponse(channel, MSG_HSO_RSP, index, null);

        } else if (type == MSG_POSITION) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            int status = buf.readUnsignedShort();

            position.setValid(BitUtil.check(status, 15));

            position.set(Position.KEY_STATUS, status);
            position.set(Position.KEY_IGNITION, BitUtil.check(status, 14));
            position.set(Position.KEY_EVENT, buf.readUnsignedShort());

            String time = buf.readSlice(12).toString(StandardCharsets.US_ASCII);

            DateBuilder dateBuilder = new DateBuilder()
                    .setYear(Integer.parseInt(time.substring(0, 2)))
                    .setMonth(Integer.parseInt(time.substring(2, 4)))
                    .setDay(Integer.parseInt(time.substring(4, 6)))
                    .setHour(Integer.parseInt(time.substring(6, 8)))
                    .setMinute(Integer.parseInt(time.substring(8, 10)))
                    .setSecond(Integer.parseInt(time.substring(10, 12)));
            position.setTime(dateBuilder.getDate());

            position.setLongitude(buf.readInt() * 0.00001);
            position.setLatitude(buf.readInt() * 0.00001);

            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
            position.setCourse(buf.readUnsignedShort());
            position.setAltitude(buf.readUnsignedShort());

            position.set(Position.KEY_ODOMETER, buf.readUnsignedShort() * 1000);

            while (buf.readableBytes() > 4) {
                buf.readUnsignedShort(); // subtype
                int length = buf.readUnsignedShort() - 4;
                buf.skipBytes(length);
            }

            sendResponse(channel, MSG_POSITION_RSP, index, null);

            return position;

        }

        return null;
    }

}
