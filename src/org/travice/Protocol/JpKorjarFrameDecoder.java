
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class JpKorjarFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 80) {
            return null;
        }

        int spaceIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ' ');
        if (spaceIndex == -1) {
            return null;
        }

        int endIndex = buf.indexOf(spaceIndex, buf.writerIndex(), (byte) ',');
        if (endIndex == -1) {
            return null;
        }

        return buf.readRetainedSlice(endIndex + 1);
    }

}
