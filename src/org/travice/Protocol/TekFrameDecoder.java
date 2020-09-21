
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;
import org.travice.helper.BitUtil;

public class TekFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 17) {
            return null;
        }

        int length = 17 + buf.getUnsignedByte(16) + (BitUtil.from(buf.getUnsignedByte(15), 6) << 6);
        if (buf.readableBytes() >= length) {
            return buf.readBytes(length);
        }

        return null;
    }

}
