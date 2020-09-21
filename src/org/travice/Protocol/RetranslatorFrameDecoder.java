
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class RetranslatorFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = 4 + buf.getIntLE(buf.readerIndex());
        if (buf.readableBytes() >= length) {
            return buf.readBytes(length);
        } else {
            return null;
        }
    }

}
