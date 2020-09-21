
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class NvsFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 4 + 2) {
            return null;
        }

        int length;
        if (buf.getUnsignedByte(buf.readerIndex()) == 0) {
            length = 2 + buf.getUnsignedShort(buf.readerIndex());
        } else {
            length = 4 + 2 + buf.getUnsignedShort(buf.readerIndex() + 4) + 2;
        }

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
