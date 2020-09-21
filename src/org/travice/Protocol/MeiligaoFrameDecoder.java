
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class MeiligaoFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_HEADER = 4;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        // Strip not '$' (0x24) bytes from the beginning
        while (buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) != 0x24) {
            buf.readByte();
        }

        // Check length and return buffer
        if (buf.readableBytes() >= MESSAGE_HEADER) {
            int length = buf.getUnsignedShort(buf.readerIndex() + 2);
            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }
        }

        return null;
    }

}
