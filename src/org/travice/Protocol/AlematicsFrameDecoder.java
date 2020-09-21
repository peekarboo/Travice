
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.travice.NetworkMessage;

public class AlematicsFrameDecoder extends LineBasedFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 2;

    public AlematicsFrameDecoder(int maxFrameLength) {
        super(maxFrameLength);
    }

    // example of heartbeat: FA F8 00 07 00 03 15 AD 4E 78 3A D2

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        if (buf.getUnsignedShort(buf.readerIndex()) == 0xFAF8) {
            ByteBuf heartbeat = buf.readRetainedSlice(12);
            if (ctx != null && ctx.channel() != null) {
                ctx.channel().writeAndFlush(new NetworkMessage(heartbeat, ctx.channel().remoteAddress()));
            }
        }

        return super.decode(ctx, buf);
    }

}
