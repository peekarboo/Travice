
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.BitUtil;
import org.travice.helper.Checksum;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class EgtsProtocolDecoder extends BaseProtocolDecoder {

    public EgtsProtocolDecoder(EgtsProtocol protocol) {
        super(protocol);
    }

    public static final int PT_RESPONSE = 0;
    public static final int PT_APPDATA = 1;
    public static final int PT_SIGNED_APPDATA = 2;

    public static final int SERVICE_AUTH = 1;
    public static final int SERVICE_TELEDATA = 2;
    public static final int SERVICE_COMMANDS = 4;
    public static final int SERVICE_FIRMWARE = 9;
    public static final int SERVICE_ECALL = 10;

    public static final int MSG_RECORD_RESPONSE = 0;
    public static final int MSG_TERM_IDENTITY = 1;
    public static final int MSG_MODULE_DATA = 2;
    public static final int MSG_VEHICLE_DATA = 3;
    public static final int MSG_AUTH_PARAMS = 4;
    public static final int MSG_AUTH_INFO = 5;
    public static final int MSG_SERVICE_INFO = 6;
    public static final int MSG_RESULT_CODE = 7;
    public static final int MSG_POS_DATA = 16;
    public static final int MSG_EXT_POS_DATA = 17;
    public static final int MSG_AD_SENSORS_DATA = 18;
    public static final int MSG_COUNTERS_DATA = 19;
    public static final int MSG_STATE_DATA = 20;
    public static final int MSG_LOOPIN_DATA = 22;
    public static final int MSG_ABS_DIG_SENS_DATA = 23;
    public static final int MSG_ABS_AN_SENS_DATA = 24;
    public static final int MSG_ABS_CNTR_DATA = 25;
    public static final int MSG_ABS_LOOPIN_DATA = 26;
    public static final int MSG_LIQUID_LEVEL_SENSOR = 27;
    public static final int MSG_PASSENGERS_COUNTERS  = 28;

    private int packetId;

    private void sendResponse(
            Channel channel, int packetType, int index, int serviceType, int type, ByteBuf content) {
        if (channel != null) {

            ByteBuf data = Unpooled.buffer();
            data.writeByte(type);
            data.writeShortLE(content.readableBytes());
            data.writeBytes(content);
            content.release();

            ByteBuf record = Unpooled.buffer();
            if (packetType == PT_RESPONSE) {
                record.writeShortLE(index);
                record.writeByte(0); // success
            }
            record.writeShortLE(data.readableBytes());
            record.writeShortLE(0);
            record.writeByte(0); // flags (possibly 1 << 6)
            record.writeByte(serviceType);
            record.writeByte(serviceType);
            record.writeBytes(data);
            data.release();
            int recordChecksum = Checksum.crc16(Checksum.CRC16_CCITT_FALSE, record.nioBuffer());

            ByteBuf response = Unpooled.buffer();
            response.writeByte(1); // protocol version
            response.writeByte(0); // security key id
            response.writeByte(0); // flags
            response.writeByte(5 + 2 + 2 + 2); // header length
            response.writeByte(0); // encoding
            response.writeShortLE(record.readableBytes());
            response.writeShortLE(packetId++);
            response.writeByte(packetType);
            response.writeByte(Checksum.crc8(Checksum.CRC8_EGTS, response.nioBuffer()));
            response.writeBytes(record);
            record.release();
            response.writeShortLE(recordChecksum);

            channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));

        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        int index = buf.getUnsignedShort(buf.readerIndex() + 5 + 2);
        buf.skipBytes(buf.getUnsignedByte(buf.readerIndex() + 3));

        List<Position> positions = new LinkedList<>();

        while (buf.readableBytes() > 2) {

            int length = buf.readUnsignedShortLE();
            int recordIndex = buf.readUnsignedShortLE();
            int recordFlags = buf.readUnsignedByte();

            if (BitUtil.check(recordFlags, 0)) {
                buf.readUnsignedIntLE(); // object id
            }

            if (BitUtil.check(recordFlags, 1)) {
                buf.readUnsignedIntLE(); // event id
            }
            if (BitUtil.check(recordFlags, 2)) {
                buf.readUnsignedIntLE(); // time
            }

            int serviceType = buf.readUnsignedByte();
            buf.readUnsignedByte(); // recipient service type

            int recordEnd = buf.readerIndex() + length;

            Position position = new Position(getProtocolName());
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession != null) {
                position.setDeviceId(deviceSession.getDeviceId());
            }

            ByteBuf response = Unpooled.buffer();
            response.writeShortLE(recordIndex);
            response.writeByte(0); // success
            sendResponse(channel, PT_RESPONSE, index, serviceType, MSG_RECORD_RESPONSE, response);

            while (buf.readerIndex() < recordEnd) {
                int type = buf.readUnsignedByte();
                int end = buf.readUnsignedShortLE() + buf.readerIndex();

                if (type == MSG_TERM_IDENTITY) {

                    buf.readUnsignedIntLE(); // object id
                    int flags = buf.readUnsignedByte();

                    if (BitUtil.check(flags, 0)) {
                        buf.readUnsignedShortLE(); // home dispatcher identifier
                    }
                    if (BitUtil.check(flags, 1)) {
                        getDeviceSession(
                                channel, remoteAddress, buf.readSlice(15).toString(StandardCharsets.US_ASCII).trim());
                    }
                    if (BitUtil.check(flags, 2)) {
                        getDeviceSession(
                                channel, remoteAddress, buf.readSlice(16).toString(StandardCharsets.US_ASCII).trim());
                    }
                    if (BitUtil.check(flags, 3)) {
                        buf.skipBytes(3); // language identifier
                    }
                    if (BitUtil.check(flags, 5)) {
                        buf.skipBytes(3); // network identifier
                    }
                    if (BitUtil.check(flags, 6)) {
                        buf.readUnsignedShortLE(); // buffer size
                    }
                    if (BitUtil.check(flags, 7)) {
                        getDeviceSession(
                                channel, remoteAddress, buf.readSlice(15).toString(StandardCharsets.US_ASCII).trim());
                    }

                    response = Unpooled.buffer();
                    response.writeByte(0); // success
                    sendResponse(channel, PT_APPDATA, 0, serviceType, MSG_RESULT_CODE, response);

                } else if (type == MSG_POS_DATA) {

                    position.setTime(new Date((buf.readUnsignedIntLE() + 1262304000) * 1000)); // since 2010-01-01
                    position.setLatitude(buf.readUnsignedIntLE() * 90.0 / 0xFFFFFFFFL);
                    position.setLongitude(buf.readUnsignedIntLE() * 180.0 / 0xFFFFFFFFL);

                    int flags = buf.readUnsignedByte();
                    position.setValid(BitUtil.check(flags, 0));
                    if (BitUtil.check(flags, 5)) {
                        position.setLatitude(-position.getLatitude());
                    }
                    if (BitUtil.check(flags, 6)) {
                        position.setLongitude(-position.getLongitude());
                    }

                    int speed = buf.readUnsignedShortLE();
                    position.setSpeed(UnitsConverter.knotsFromKph(BitUtil.to(speed, 14) * 0.1));
                    position.setCourse(buf.readUnsignedByte() + (BitUtil.check(speed, 15) ? 0x100 : 0));

                    position.set(Position.KEY_ODOMETER, buf.readUnsignedMediumLE() * 100);
                    position.set(Position.KEY_INPUT, buf.readUnsignedByte());
                    position.set(Position.KEY_EVENT, buf.readUnsignedByte());

                    if (BitUtil.check(flags, 7)) {
                        position.setAltitude(buf.readMediumLE());
                    }

                } else if (type == MSG_EXT_POS_DATA) {

                    int flags = buf.readUnsignedByte();

                    if (BitUtil.check(flags, 0)) {
                        position.set(Position.KEY_VDOP, buf.readUnsignedShortLE());
                    }
                    if (BitUtil.check(flags, 1)) {
                        position.set(Position.KEY_HDOP, buf.readUnsignedShortLE());
                    }
                    if (BitUtil.check(flags, 2)) {
                        position.set(Position.KEY_PDOP, buf.readUnsignedShortLE());
                    }
                    if (BitUtil.check(flags, 3)) {
                        position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());
                    }

                } else if (type == MSG_AD_SENSORS_DATA) {

                    buf.readUnsignedByte(); // inputs flags

                    position.set(Position.KEY_OUTPUT, buf.readUnsignedByte());

                    buf.readUnsignedByte(); // adc flags

                }

                buf.readerIndex(end);
            }

            if (serviceType == SERVICE_TELEDATA && deviceSession != null) {
                positions.add(position);
            }
        }

        return positions.isEmpty() ? null : positions;
    }

}
