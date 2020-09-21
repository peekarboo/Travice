
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import org.travice.BaseFrameDecoder;
import org.travice.helper.BufferUtil;

public class TotemFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        int beginIndex = BufferUtil.indexOf("$$", buf);
        if (beginIndex == -1) {
            return null;
        } else if (beginIndex > buf.readerIndex()) {
            buf.readerIndex(beginIndex);
        }

        int length;

        if (buf.getByte(buf.readerIndex() + 2) == (byte) '0') {
            length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 4, StandardCharsets.US_ASCII));
        } else {
            length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 2, StandardCharsets.US_ASCII), 16);
        }

        if (length <= buf.readableBytes()) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
