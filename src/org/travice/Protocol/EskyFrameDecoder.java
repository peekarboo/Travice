
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class EskyFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        buf.readerIndex(buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 'E'));

        int endIndex = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte) 'E');
        if (endIndex > 0) {
            return buf.readRetainedSlice(endIndex - buf.readerIndex());
        } else {
            return buf.readRetainedSlice(buf.readableBytes()); // assume full frame
        }
    }

}
