
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.BitUtil;
import org.travice.helper.UnitsConverter;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BceProtocolDecoder extends BaseProtocolDecoder {

    public BceProtocolDecoder(BceProtocol protocol) {
        super(protocol);
    }

    private static final int DATA_TYPE = 7;

    public static final int MSG_ASYNC_STACK = 0xA5;
    public static final int MSG_STACK_COFIRM = 0x19;
    public static final int MSG_TIME_TRIGGERED = 0xA0;
    public static final int MSG_OUTPUT_CONTROL = 0x41;
    public static final int MSG_OUTPUT_CONTROL_ACK = 0xC1;

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        String imei = String.format("%015d", buf.readLongLE());
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        List<Position> positions = new LinkedList<>();

        while (buf.readableBytes() > 1) {

            int dataEnd = buf.readUnsignedShortLE() + buf.readerIndex();
            int type = buf.readUnsignedByte();
            int confirmKey = buf.readUnsignedByte() & 0x7F;

            while (buf.readerIndex() < dataEnd) {

                Position position = new Position(getProtocolName());
                position.setDeviceId(deviceSession.getDeviceId());

                int structEnd = buf.readUnsignedByte() + buf.readerIndex();

                long time = buf.readUnsignedIntLE();
                if ((time & 0x0f) == DATA_TYPE) {

                    time = time >> 4 << 1;
                    time += 0x47798280; // 01/01/2008
                    position.setTime(new Date(time * 1000));

                    // Read masks
                    int mask;
                    List<Integer> masks = new LinkedList<>();
                    do {
                        mask = buf.readUnsignedShortLE();
                        masks.add(mask);
                    } while (BitUtil.check(mask, 15));

                    mask = masks.get(0);

                    if (BitUtil.check(mask, 0)) {
                        position.setValid(true);
                        position.setLongitude(buf.readFloatLE());
                        position.setLatitude(buf.readFloatLE());
                        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

                        int status = buf.readUnsignedByte();
                        position.set(Position.KEY_SATELLITES, BitUtil.to(status, 4));
                        position.set(Position.KEY_HDOP, BitUtil.from(status, 4));

                        position.setCourse(buf.readUnsignedByte() * 2);
                        position.setAltitude(buf.readUnsignedShortLE());

                        position.set(Position.KEY_ODOMETER, buf.readUnsignedIntLE());
                    }

                    if (BitUtil.check(mask, 1)) {
                        position.set(Position.KEY_INPUT, buf.readUnsignedShortLE());
                    }

                    for (int i = 1; i <= 8; i++) {
                        if (BitUtil.check(mask, i + 1)) {
                            position.set(Position.PREFIX_ADC + i, buf.readUnsignedShortLE());
                        }
                    }

                    if (BitUtil.check(mask, 10)) {
                        buf.skipBytes(4);
                    }
                    if (BitUtil.check(mask, 11)) {
                        buf.skipBytes(4);
                    }
                    if (BitUtil.check(mask, 12)) {
                        buf.skipBytes(2);
                    }
                    if (BitUtil.check(mask, 13)) {
                        buf.skipBytes(2);
                    }

                    if (BitUtil.check(mask, 14)) {
                        position.setNetwork(new Network(CellTower.from(
                                buf.readUnsignedShortLE(), buf.readUnsignedByte(),
                                buf.readUnsignedShortLE(), buf.readUnsignedShortLE(),
                                buf.readUnsignedByte())));
                        buf.readUnsignedByte();
                    }

                    if (BitUtil.check(mask, 0)) {
                        positions.add(position);
                    }
                }

                buf.readerIndex(structEnd);
            }

            // Send response
            if (type == MSG_ASYNC_STACK && channel != null) {
                ByteBuf response = Unpooled.buffer(8 + 2 + 2 + 1);
                response.writeLongLE(Long.parseLong(imei));
                response.writeShortLE(2);
                response.writeByte(MSG_STACK_COFIRM);
                response.writeByte(confirmKey);

                int checksum = 0;
                for (int i = 0; i < response.writerIndex(); i++) {
                    checksum += response.getUnsignedByte(i);
                }
                response.writeByte(checksum);

                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }
        }

        return positions;
    }

}
