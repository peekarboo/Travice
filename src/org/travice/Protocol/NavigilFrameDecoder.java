
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class NavigilFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_HEADER = 20;
    private static final long PREAMBLE = 0x2477F5F6;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        // Check minimum length
        if (buf.readableBytes() < MESSAGE_HEADER) {
            return null;
        }

        // Check for preamble
        boolean hasPreamble = false;
        if (buf.getUnsignedIntLE(buf.readerIndex()) == PREAMBLE) {
            hasPreamble = true;
        }

        // Check length and return buffer
        int length = buf.getUnsignedShortLE(buf.readerIndex() + 6);
        if (buf.readableBytes() >= length) {
            if (hasPreamble) {
                buf.readUnsignedIntLE();
                length -= 4;
            }
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
