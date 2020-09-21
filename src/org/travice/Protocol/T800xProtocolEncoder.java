
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.DataConverter;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.charset.StandardCharsets;

public class T800xProtocolEncoder extends BaseProtocolEncoder {

    public static final int MODE_SETTING = 0x01;
    public static final int MODE_BROADCAST = 0x02;
    public static final int MODE_FORWARD = 0x03;

    private ByteBuf encodeContent(Command command, String content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeByte('#');
        buf.writeByte('#');
        buf.writeByte(T800xProtocolDecoder.MSG_COMMAND);
        buf.writeShort(7 + 8 + 1 + content.length());
        buf.writeShort(1); // serial number
        buf.writeBytes(DataConverter.parseHex("0" + getUniqueId(command.getDeviceId())));
        buf.writeByte(MODE_SETTING);
        buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return encodeContent(command, command.getString(Command.KEY_DATA));
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
