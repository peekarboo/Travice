
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.DataConverter;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class EelinkProtocolEncoder extends BaseProtocolEncoder {

    private boolean connectionless;

    public EelinkProtocolEncoder(boolean connectionless) {
        this.connectionless = connectionless;
    }

    public static int checksum(ByteBuffer buf) {
        int sum = 0;
        while (buf.hasRemaining()) {
            sum = (((sum << 1) | (sum >> 15)) + (buf.get() & 0xFF)) & 0xFFFF;
        }
        return sum;
    }

    public static ByteBuf encodeContent(
            boolean connectionless, String uniqueId, int type, int index, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer();

        if (connectionless) {
            buf.writeBytes(DataConverter.parseHex('0' + uniqueId));
        }

        buf.writeByte(0x67);
        buf.writeByte(0x67);
        buf.writeByte(type);
        buf.writeShort(2 + (content != null ? content.readableBytes() : 0)); // length
        buf.writeShort(index);

        if (content != null) {
            buf.writeBytes(content);
        }

        ByteBuf result = Unpooled.buffer();

        if (connectionless) {
            result.writeByte('E');
            result.writeByte('L');
            result.writeShort(2 + buf.readableBytes()); // length
            result.writeShort(checksum(buf.nioBuffer()));
        }

        result.writeBytes(buf);

        return result;
    }

    private ByteBuf encodeContent(long deviceId, String content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(0x01); // command
        buf.writeInt(0); // server id
        buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));

        return encodeContent(connectionless, getUniqueId(deviceId), EelinkProtocolDecoder.MSG_DOWNLINK, 0, buf);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return encodeContent(command.getDeviceId(), command.getString(Command.KEY_DATA));
            case Command.TYPE_POSITION_SINGLE:
                return encodeContent(command.getDeviceId(), "WHERE#");
            case Command.TYPE_ENGINE_STOP:
                return encodeContent(command.getDeviceId(), "RELAY,1#");
            case Command.TYPE_ENGINE_RESUME:
                return encodeContent(command.getDeviceId(), "RELAY,0#");
            case Command.TYPE_REBOOT_DEVICE:
                return encodeContent(command.getDeviceId(), "RESET#");
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
