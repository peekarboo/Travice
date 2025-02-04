
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.DataConverter;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RuptelaProtocolDecoder extends BaseProtocolDecoder {

    public RuptelaProtocolDecoder(RuptelaProtocol protocol) {
        super(protocol);
    }

    public static final int MSG_RECORDS = 1;
    public static final int MSG_DEVICE_CONFIGURATION = 2;
    public static final int MSG_DEVICE_VERSION = 3;
    public static final int MSG_FIRMWARE_UPDATE = 4;
    public static final int MSG_SET_CONNECTION = 5;
    public static final int MSG_SET_ODOMETER = 6;
    public static final int MSG_SMS_VIA_GPRS_RESPONSE = 7;
    public static final int MSG_SMS_VIA_GPRS = 8;
    public static final int MSG_DTCS = 9;
    public static final int MSG_SET_IO = 17;
    public static final int MSG_EXTENDED_RECORDS = 68;

    private Position decodeCommandResponse(DeviceSession deviceSession, int type, ByteBuf buf) {
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        getLastLocation(position, null);

        position.set(Position.KEY_TYPE, type);

        switch (type) {
            case MSG_DEVICE_CONFIGURATION:
            case MSG_DEVICE_VERSION:
            case MSG_FIRMWARE_UPDATE:
            case MSG_SMS_VIA_GPRS_RESPONSE:
                position.set(Position.KEY_RESULT,
                        buf.toString(buf.readerIndex(), buf.readableBytes() - 2, StandardCharsets.US_ASCII).trim());
                return position;
            case MSG_SET_IO:
                position.set(Position.KEY_RESULT,
                        String.valueOf(buf.readUnsignedByte()));
                return position;
            default:
                return null;
        }
    }

    private long readValue(ByteBuf buf, int length, boolean signed) {
        switch (length) {
            case 1:
                return signed ? buf.readByte() : buf.readUnsignedByte();
            case 2:
                return signed ? buf.readShort() : buf.readUnsignedShort();
            case 4:
                return signed ? buf.readInt() : buf.readUnsignedInt();
            default:
                return buf.readLong();
        }
    }

    private void decodeParameter(Position position, int id, ByteBuf buf, int length) {
        switch (id) {
            case 2:
            case 3:
            case 4:
                position.set("di" + (id - 1), readValue(buf, length, false));
                break;
            case 5:
                position.set(Position.KEY_IGNITION, readValue(buf, length, false) == 1);
                break;
            case 74:
                position.set(Position.PREFIX_TEMP + 3, readValue(buf, length, true) * 0.1);
                break;
            case 78:
            case 79:
            case 80:
                position.set(Position.PREFIX_TEMP + (id - 78), readValue(buf, length, true) * 0.1);
                break;
            default:
                position.set(Position.PREFIX_IO + id, readValue(buf, length, false));
                break;
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedShort(); // data length

        String imei = String.format("%015d", buf.readLong());
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        int type = buf.readUnsignedByte();

        if (type == MSG_RECORDS || type == MSG_EXTENDED_RECORDS) {

            List<Position> positions = new LinkedList<>();

            buf.readUnsignedByte(); // records left
            int count = buf.readUnsignedByte();

            for (int i = 0; i < count; i++) {
                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                position.setTime(new Date(buf.readUnsignedInt() * 1000));
                buf.readUnsignedByte(); // timestamp extension

                if (type == MSG_EXTENDED_RECORDS) {
                    buf.readUnsignedByte(); // record extension
                }

                buf.readUnsignedByte(); // priority (reserved)

                position.setValid(true);
                position.setLongitude(buf.readInt() / 10000000.0);
                position.setLatitude(buf.readInt() / 10000000.0);
                position.setAltitude(buf.readUnsignedShort() / 10.0);
                position.setCourse(buf.readUnsignedShort() / 100.0);

                position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());

                position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));

                position.set(Position.KEY_HDOP, buf.readUnsignedByte() / 10.0);

                if (type == MSG_EXTENDED_RECORDS) {
                    position.set(Position.KEY_EVENT, buf.readUnsignedShort());
                } else {
                    position.set(Position.KEY_EVENT, buf.readUnsignedByte());
                }

                // Read 1 byte data
                int cnt = buf.readUnsignedByte();
                for (int j = 0; j < cnt; j++) {
                    int id = type == MSG_EXTENDED_RECORDS ? buf.readUnsignedShort() : buf.readUnsignedByte();
                    decodeParameter(position, id, buf, 1);
                }

                // Read 2 byte data
                cnt = buf.readUnsignedByte();
                for (int j = 0; j < cnt; j++) {
                    int id = type == MSG_EXTENDED_RECORDS ? buf.readUnsignedShort() : buf.readUnsignedByte();
                    decodeParameter(position, id, buf, 2);
                }

                // Read 4 byte data
                cnt = buf.readUnsignedByte();
                for (int j = 0; j < cnt; j++) {
                    int id = type == MSG_EXTENDED_RECORDS ? buf.readUnsignedShort() : buf.readUnsignedByte();
                    decodeParameter(position, id, buf, 4);
                }

                // Read 8 byte data
                cnt = buf.readUnsignedByte();
                for (int j = 0; j < cnt; j++) {
                    int id = type == MSG_EXTENDED_RECORDS ? buf.readUnsignedShort() : buf.readUnsignedByte();
                    decodeParameter(position, id, buf, 8);
                }

                positions.add(position);
            }

            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage(
                        Unpooled.wrappedBuffer(DataConverter.parseHex("0002640113bc")), remoteAddress));
            }

            return positions;

        } else if (type == MSG_DTCS) {

            List<Position> positions = new LinkedList<>();

            int count = buf.readUnsignedByte();

            for (int i = 0; i < count; i++) {
                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                buf.readUnsignedByte(); // reserved

                position.setTime(new Date(buf.readUnsignedInt() * 1000));

                position.setValid(true);
                position.setLongitude(buf.readInt() / 10000000.0);
                position.setLatitude(buf.readInt() / 10000000.0);

                if (buf.readUnsignedByte() == 2) {
                    position.set(Position.KEY_ARCHIVE, true);
                }

                position.set(Position.KEY_DTCS, buf.readSlice(5).toString(StandardCharsets.US_ASCII));

                positions.add(position);
            }

            if (channel != null) {
                channel.writeAndFlush(new NetworkMessage(
                        Unpooled.wrappedBuffer(DataConverter.parseHex("00026d01c4a4")), remoteAddress));
            }

            return positions;

        } else {

            return decodeCommandResponse(deviceSession, type, buf);

        }
    }

}
