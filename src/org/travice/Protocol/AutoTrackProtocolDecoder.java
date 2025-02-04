
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Checksum;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.Date;

public class AutoTrackProtocolDecoder extends BaseProtocolDecoder {

    public AutoTrackProtocolDecoder(AutoTrackProtocol protocol) {
        super(protocol);
    }

    public static final int MSG_LOGIN_REQUEST = 51;
    public static final int MSG_LOGIN_CONFIRM = 101;
    public static final int MSG_TELEMETRY_1 = 52;
    public static final int MSG_TELEMETRY_2 = 66;
    public static final int MSG_TELEMETRY_3 = 67;
    public static final int MSG_KEEP_ALIVE = 114;

    private Position decodeTelemetry(DeviceSession deviceSession, ByteBuf buf) {

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(new Date(1007078400000L + buf.readUnsignedIntLE() * 1000)); // seconds since 2002
        position.setLatitude(buf.readIntLE() * 0.0000001);
        position.setLongitude(buf.readIntLE() * 0.0000001);

        position.set(Position.KEY_ODOMETER, buf.readUnsignedIntLE());
        position.set(Position.KEY_FUEL_USED, buf.readUnsignedIntLE());

        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));

        position.set(Position.KEY_INPUT, buf.readUnsignedShortLE());
        buf.readUnsignedIntLE(); // di 3 count
        buf.readUnsignedIntLE(); // di 4 count

        for (int i = 0; i < 5; i++) {
            position.set(Position.PREFIX_ADC + (i + 1), buf.readUnsignedShortLE());
        }

        position.setCourse(buf.readUnsignedShortLE());

        position.set(Position.KEY_STATUS, buf.readUnsignedShortLE());
        position.set(Position.KEY_EVENT, buf.readUnsignedShortLE());
        position.set(Position.KEY_DRIVER_UNIQUE_ID, buf.readLongLE());
        position.set(Position.KEY_INDEX, buf.readUnsignedShortLE());

        buf.readUnsignedShortLE(); // checksum

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.skipBytes(4); // sync
        int type = buf.readUnsignedByte();
        buf.readUnsignedShortLE(); // length

        switch (type) {
            case MSG_LOGIN_REQUEST:
                String imei = ByteBufUtil.hexDump(buf.readBytes(8));
                DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
                if (deviceSession == null) {
                    return null;
                }
                int fuelConst = buf.readUnsignedShortLE();
                int tripConst = buf.readUnsignedShortLE();
                if (channel != null) {
                    ByteBuf response = Unpooled.buffer();
                    response.writeInt(0xF1F1F1F1); // sync
                    response.writeByte(MSG_LOGIN_CONFIRM);
                    response.writeShortLE(12); // length
                    response.writeBytes(ByteBufUtil.decodeHexDump(imei));
                    response.writeShortLE(fuelConst);
                    response.writeShortLE(tripConst);
                    response.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, response.nioBuffer()));
                    channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
                }
                return null;
            case MSG_TELEMETRY_1:
            case MSG_TELEMETRY_2:
            case MSG_TELEMETRY_3:
                deviceSession = getDeviceSession(channel, remoteAddress);
                if (deviceSession == null) {
                    return null;
                }
                return decodeTelemetry(deviceSession, buf);
            default:
                return null;
        }
    }

}
