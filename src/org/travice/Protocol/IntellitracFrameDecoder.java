
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.travice.NetworkMessage;

public class IntellitracFrameDecoder extends LineBasedFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 0;

    public IntellitracFrameDecoder(int maxFrameLength) {
        super(maxFrameLength);
    }

    // example of sync header: 0xFA 0xF8 0x1B 0x01 0x81 0x60 0x33 0x3C

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

        // Check minimum length
        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        // Check for sync packet
        if (buf.getUnsignedShort(buf.readerIndex()) == 0xFAF8) {
            ByteBuf syncMessage = buf.readRetainedSlice(8);
            if (ctx != null && ctx.channel() != null) {
                ctx.channel().writeAndFlush(new NetworkMessage(syncMessage, ctx.channel().remoteAddress()));
            }
        }

        return super.decode(ctx, buf);
    }

}
