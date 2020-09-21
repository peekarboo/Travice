
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class RetranslatorProtocolDecoder extends BaseProtocolDecoder {

    public RetranslatorProtocolDecoder(RetranslatorProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(new byte[]{0x11}), remoteAddress));
        }

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedInt(); // length

        int idLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x00) - buf.readerIndex();
        String id = buf.readBytes(idLength).toString(StandardCharsets.US_ASCII);
        buf.readByte();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setTime(new Date(buf.readUnsignedInt() * 1000));

        buf.readUnsignedInt(); // bit flags

        while (buf.isReadable()) {

            buf.readUnsignedShort(); // block type
            int blockEnd = buf.readInt() + buf.readerIndex();
            buf.readUnsignedByte(); // security attribute
            int dataType = buf.readUnsignedByte();

            int nameLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x00) - buf.readerIndex();
            String name = buf.readBytes(nameLength).toString(StandardCharsets.US_ASCII);
            buf.readByte();

            if (name.equals("posinfo")) {
                position.setValid(true);
                position.setLongitude(buf.readDoubleLE());
                position.setLatitude(buf.readDoubleLE());
                position.setAltitude(buf.readDoubleLE());
                position.setSpeed(buf.readShort());
                position.setCourse(buf.readShort());
                position.set(Position.KEY_SATELLITES, buf.readByte());
            } else {
                switch (dataType) {
                    case 1:
                        int len = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x00) - buf.readerIndex();
                        position.set(name, buf.readBytes(len).toString(StandardCharsets.US_ASCII));
                        buf.readByte();
                        break;
                    case 3:
                        position.set(name, buf.readInt());
                        break;
                    case 4:
                        position.set(name, buf.readDoubleLE());
                        break;
                    case 5:
                        position.set(name, buf.readLong());
                        break;
                    default:
                        break;
                }
            }

            buf.readerIndex(blockEnd);

        }

        if (position.getLatitude() == 0 && position.getLongitude() == 0) {
            getLastLocation(position, position.getDeviceTime());
        }

        return position;
    }

}
