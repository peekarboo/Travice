
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class SabertekFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int beginIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x02);
        if (beginIndex >= 0) {
            int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0x03);
            if (endIndex >= 0) {
                buf.readerIndex(beginIndex + 1);
                ByteBuf frame = buf.readRetainedSlice(endIndex - beginIndex - 1);
                buf.readerIndex(endIndex + 1);
                buf.skipBytes(2); // end line
                return frame;
            }
        }

        return null;
    }

}
