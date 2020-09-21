
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class CellocatorProtocolEncoder extends BaseProtocolEncoder {

    private ByteBuf encodeContent(long deviceId, int command, int data1, int data2) {

        ByteBuf buf = Unpooled.buffer(0);
        buf.writeByte('M');
        buf.writeByte('C');
        buf.writeByte('G');
        buf.writeByte('P');
        buf.writeByte(0);
        buf.writeIntLE(Integer.parseInt(getUniqueId(deviceId)));
        buf.writeByte(0); // command numerator
        buf.writeIntLE(0); // authentication code
        buf.writeByte(command);
        buf.writeByte(command);
        buf.writeByte(data1);
        buf.writeByte(data1);
        buf.writeByte(data2);
        buf.writeByte(data2);
        buf.writeIntLE(0); // command specific data

        byte checksum = 0;
        for (int i = 4; i < buf.writerIndex(); i++) {
            checksum += buf.getByte(i);
        }
        buf.writeByte(checksum);

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_OUTPUT_CONTROL:
                int data = Integer.parseInt(command.getString(Command.KEY_DATA)) << 4
                        + command.getInteger(Command.KEY_INDEX);
                return encodeContent(command.getDeviceId(), 0x03, data, 0);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
