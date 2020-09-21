
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.charset.StandardCharsets;

public class NoranProtocolEncoder extends BaseProtocolEncoder {

    private ByteBuf encodeContent(String content) {

        ByteBuf buf = Unpooled.buffer(12 + 56);

        buf.writeCharSequence("\r\n*KW", StandardCharsets.US_ASCII);
        buf.writeByte(0);
        buf.writeShortLE(buf.capacity());
        buf.writeShortLE(NoranProtocolDecoder.MSG_CONTROL);
        buf.writeInt(0); // gis ip
        buf.writeShortLE(0); // gis port
        buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
        buf.writerIndex(buf.writerIndex() + 50 - content.length());
        buf.writeCharSequence("\r\n", StandardCharsets.US_ASCII);

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_POSITION_SINGLE:
                return encodeContent("*KW,000,000,000000#");
            case Command.TYPE_POSITION_PERIODIC:
                int interval = command.getInteger(Command.KEY_FREQUENCY);
                return encodeContent("*KW,000,002,000000," + interval + "#");
            case Command.TYPE_POSITION_STOP:
                return encodeContent("*KW,000,002,000000,0#");
            case Command.TYPE_ENGINE_STOP:
                return encodeContent("*KW,000,007,000000,0#");
            case Command.TYPE_ENGINE_RESUME:
                return encodeContent("*KW,000,007,000000,1#");
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
