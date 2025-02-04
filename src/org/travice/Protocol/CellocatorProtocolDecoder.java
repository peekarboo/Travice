
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.DateBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;

public class CellocatorProtocolDecoder extends BaseProtocolDecoder {

    public CellocatorProtocolDecoder(CellocatorProtocol protocol) {
        super(protocol);
    }

    static final int MSG_CLIENT_STATUS = 0;
    static final int MSG_CLIENT_PROGRAMMING = 3;
    static final int MSG_CLIENT_SERIAL_LOG = 7;
    static final int MSG_CLIENT_SERIAL = 8;
    static final int MSG_CLIENT_MODULAR = 9;

    public static final int MSG_SERVER_ACKNOWLEDGE = 4;

    private byte commandCount;

    private void sendReply(Channel channel, SocketAddress remoteAddress, long deviceId, byte packetNumber) {
        if (channel != null) {
            ByteBuf reply = Unpooled.buffer(28);
            reply.writeByte('M');
            reply.writeByte('C');
            reply.writeByte('G');
            reply.writeByte('P');
            reply.writeByte(MSG_SERVER_ACKNOWLEDGE);
            reply.writeIntLE((int) deviceId);
            reply.writeByte(commandCount++);
            reply.writeIntLE(0); // authentication code
            reply.writeByte(0);
            reply.writeByte(packetNumber);
            reply.writeZero(11);

            byte checksum = 0;
            for (int i = 4; i < 27; i++) {
                checksum += reply.getByte(i);
            }
            reply.writeByte(checksum);

            channel.writeAndFlush(new NetworkMessage(reply, remoteAddress));
        }
    }

    private String decodeAlarm(short reason) {
        switch (reason) {
            case 70:
                return Position.ALARM_SOS;
            case 80:
                return Position.ALARM_POWER_CUT;
            case 81:
                return Position.ALARM_LOW_POWER;
            default:
                return null;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(4); // system code
        int type = buf.readUnsignedByte();
        long deviceUniqueId = buf.readUnsignedIntLE();

        if (type != MSG_CLIENT_SERIAL) {
            buf.readUnsignedShortLE(); // communication control
        }
        byte packetNumber = buf.readByte();

        sendReply(channel, remoteAddress, deviceUniqueId, packetNumber);

        if (type == MSG_CLIENT_STATUS) {

            Position position = new Position(getProtocolName());

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, String.valueOf(deviceUniqueId));
            if (deviceSession == null) {
                return null;
            }
            position.setDeviceId(deviceSession.getDeviceId());

            position.set(Position.KEY_VERSION_HW, buf.readUnsignedByte());
            position.set(Position.KEY_VERSION_FW, buf.readUnsignedByte());
            position.set("protocolVersion", buf.readUnsignedByte());

            position.set(Position.KEY_STATUS, buf.getUnsignedByte(buf.readerIndex()) & 0x0f);

            int operator = (buf.readUnsignedByte() & 0xf0) << 4;
            operator += buf.readUnsignedByte();

            buf.readUnsignedByte(); // reason data
            position.set(Position.KEY_ALARM, decodeAlarm(buf.readUnsignedByte()));

            position.set("mode", buf.readUnsignedByte());
            position.set(Position.PREFIX_IO + 1, buf.readUnsignedIntLE());

            operator <<= 8;
            operator += buf.readUnsignedByte();
            position.set(Position.KEY_OPERATOR, operator);

            position.set(Position.PREFIX_ADC + 1, buf.readUnsignedIntLE());
            position.set(Position.KEY_ODOMETER, buf.readUnsignedMediumLE());
            buf.skipBytes(6); // multi-purpose data

            position.set(Position.KEY_GPS, buf.readUnsignedShortLE());
            position.set("locationStatus", buf.readUnsignedByte());
            position.set("mode1", buf.readUnsignedByte());
            position.set("mode2", buf.readUnsignedByte());

            position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());

            position.setValid(true);
            position.setLongitude(buf.readIntLE() / Math.PI * 180 / 100000000);
            position.setLatitude(buf.readIntLE() / Math.PI * 180 / 100000000.0);
            position.setAltitude(buf.readIntLE() * 0.01);
            position.setSpeed(UnitsConverter.knotsFromMps(buf.readIntLE() * 0.01));
            position.setCourse(buf.readUnsignedShortLE() / Math.PI * 180.0 / 1000.0);

            DateBuilder dateBuilder = new DateBuilder()
                    .setTimeReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                    .setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedShortLE());
            position.setTime(dateBuilder.getDate());

            return position;
        }

        return null;
    }

}
