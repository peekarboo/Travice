
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Command;

public class KhdProtocolEncoder extends BaseProtocolEncoder {

    public static final int MSG_CUT_OIL = 0x39;
    public static final int MSG_RESUME_OIL = 0x38;

    private ByteBuf encodeCommand(int command, String uniqueId) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(0x29);
        buf.writeByte(0x29);

        buf.writeByte(command);
        buf.writeShort(6); // size

        uniqueId = "00000000".concat(uniqueId);
        uniqueId = uniqueId.substring(uniqueId.length() - 8);
        buf.writeByte(Integer.parseInt(uniqueId.substring(0, 2)));
        buf.writeByte(Integer.parseInt(uniqueId.substring(2, 4)) + 0x80);
        buf.writeByte(Integer.parseInt(uniqueId.substring(4, 6)) + 0x80);
        buf.writeByte(Integer.parseInt(uniqueId.substring(6, 8)));

        buf.writeByte(Checksum.xor(buf.nioBuffer()));
        buf.writeByte(0x0D); // ending

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        String uniqueId = getUniqueId(command.getDeviceId());

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return encodeCommand(MSG_CUT_OIL, uniqueId);
            case Command.TYPE_ENGINE_RESUME:
                return encodeCommand(MSG_RESUME_OIL, uniqueId);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
