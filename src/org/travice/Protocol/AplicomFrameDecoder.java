
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class AplicomFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        // Skip Alive message
        while (buf.isReadable() && Character.isDigit(buf.getByte(buf.readerIndex()))) {
            buf.readByte();
        }

        // Check minimum length
        if (buf.readableBytes() < 11) {
            return null;
        }

        // Read flags
        int version = buf.getUnsignedByte(buf.readerIndex() + 1);
        int offset = 1 + 1 + 3;
        if ((version & 0x80) != 0) {
            offset += 4;
        }

        // Get data length
        int length = buf.getUnsignedShort(buf.readerIndex() + offset);
        offset += 2;
        if ((version & 0x40) != 0) {
            offset += 3;
        }
        length += offset; // add header

        // Return buffer
        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
