
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;

public class M2mProtocolDecoder extends BaseProtocolDecoder {

    public M2mProtocolDecoder(M2mProtocol protocol) {
        super(protocol);
    }

    private boolean firstPacket = true;

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        // Remove offset
        for (int i = 0; i < buf.readableBytes(); i++) {
            int b = buf.getByte(i);
            if (b != 0x0b) {
                buf.setByte(i, b - 0x20);
            }
        }

        if (firstPacket) {

            firstPacket = false;

            StringBuilder imei = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                int b = buf.readByte();
                if (i != 0) {
                    imei.append(b / 10);
                }
                imei.append(b % 10);
            }

            getDeviceSession(channel, remoteAddress, imei.toString());

        } else {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            DateBuilder dateBuilder = new DateBuilder()
                    .setDay(buf.readUnsignedByte() & 0x3f)
                    .setMonth(buf.readUnsignedByte() & 0x3f)
                    .setYear(buf.readUnsignedByte())
                    .setHour(buf.readUnsignedByte() & 0x3f)
                    .setMinute(buf.readUnsignedByte() & 0x7f)
                    .setSecond(buf.readUnsignedByte() & 0x7f);
            position.setTime(dateBuilder.getDate());

            int degrees = buf.readUnsignedByte();
            double latitude = buf.readUnsignedByte();
            latitude += buf.readUnsignedByte() / 100.0;
            latitude += buf.readUnsignedByte() / 10000.0;
            latitude /= 60;
            latitude += degrees;

            int b = buf.readUnsignedByte();

            degrees = (b & 0x7f) * 100 + buf.readUnsignedByte();
            double longitude = buf.readUnsignedByte();
            longitude += buf.readUnsignedByte() / 100.0;
            longitude += buf.readUnsignedByte() / 10000.0;
            longitude /= 60;
            longitude += degrees;

            if ((b & 0x80) != 0) {
                longitude = -longitude;
            }
            if ((b & 0x40) != 0) {
                latitude = -latitude;
            }

            position.setValid(true);
            position.setLatitude(latitude);
            position.setLongitude(longitude);
            position.setSpeed(buf.readUnsignedByte());

            int satellites = buf.readUnsignedByte();
            if (satellites == 0) {
                return null; // cell information
            }
            position.set(Position.KEY_SATELLITES, satellites);

            // decode other data

            return position;

        }

        return null;
    }

}
