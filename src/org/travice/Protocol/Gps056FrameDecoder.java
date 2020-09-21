
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

import java.nio.charset.StandardCharsets;

public class Gps056FrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_HEADER = 4;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() >= MESSAGE_HEADER) {
            int length = Integer.parseInt(buf.toString(2, 2, StandardCharsets.US_ASCII)) + 5;
            if (buf.readableBytes() >= length) {
                ByteBuf frame = buf.readRetainedSlice(length);
                while (buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) != '$') {
                    buf.readByte();
                }
                return frame;
            }
        }

        return null;
    }

}
