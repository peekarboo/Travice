
package org.travice.protocol;

import org.travice.BaseProtocolEncoder;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class TeltonikaProtocolEncoder extends BaseProtocolEncoder {

    private ByteBuf encodeContent(String content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeInt(0);
        buf.writeInt(content.length() + 10);
        buf.writeByte(TeltonikaProtocolDecoder.CODEC_12);
        buf.writeByte(1); // quantity
        buf.writeByte(5); // type
        buf.writeInt(content.length() + 2);
        buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
        buf.writeByte('\r');
        buf.writeByte('\n');
        buf.writeByte(1); // quantity
        buf.writeInt(Checksum.crc16(Checksum.CRC16_IBM, buf.nioBuffer(8, buf.writerIndex() - 8)));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return encodeContent(command.getString(Command.KEY_DATA));
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
