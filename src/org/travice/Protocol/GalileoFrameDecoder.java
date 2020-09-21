
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class GalileoFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 5;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        int length = buf.getUnsignedShortLE(buf.readerIndex() + 1) & 0x7fff;
        if (buf.readableBytes() >= (length + MESSAGE_MINIMUM_LENGTH)) {
            return buf.readRetainedSlice(length + MESSAGE_MINIMUM_LENGTH);
        }

        return null;
    }

}
