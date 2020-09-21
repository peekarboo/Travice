
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

import java.nio.charset.StandardCharsets;

public class T57FrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        String type = buf.toString(buf.readerIndex() + 5, 2, StandardCharsets.US_ASCII);
        int count = type.equals("F3") ? 12 : 14;

        int index = 0;
        while (index >= 0 && count > 0) {
            index = buf.indexOf(index + 1, buf.writerIndex(), (byte) '#');
            if (index > 0) {
                count -= 1;
            }
        }

        return index > 0 ? buf.readRetainedSlice(index + 1 - buf.readerIndex()) : null;
    }

}
