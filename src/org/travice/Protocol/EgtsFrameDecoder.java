
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class EgtsFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        int headerLength = buf.getUnsignedByte(buf.readerIndex() + 3);
        int frameLength = buf.getUnsignedShortLE(buf.readerIndex() + 5);

        int length = headerLength + frameLength + (frameLength > 0 ? 2 : 0);

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
