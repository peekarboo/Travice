
package org.travice.protocol;

import org.travice.BaseFrameDecoder;
import org.travice.NetworkMessage;
import org.travice.helper.BufferUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class WondexFrameDecoder extends BaseFrameDecoder {

    private static final int KEEP_ALIVE_LENGTH = 8;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < KEEP_ALIVE_LENGTH) {
            return null;
        }

        if (buf.getUnsignedByte(buf.readerIndex()) == 0xD0) {

            // Send response
            ByteBuf frame = buf.readRetainedSlice(KEEP_ALIVE_LENGTH);
            if (channel != null) {
                frame.retain();
                channel.writeAndFlush(new NetworkMessage(frame, channel.remoteAddress()));
            }
            return frame;

        } else {

            int index = BufferUtil.indexOf("\r\n", buf);
            if (index != -1) {
                ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
                buf.skipBytes(2);
                return frame;
            }

        }

        return null;
    }

}
