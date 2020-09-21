
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;
import org.travice.helper.BufferUtil;

import java.nio.charset.StandardCharsets;

public class MegastekFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        if (Character.isDigit(buf.getByte(buf.readerIndex()))) {
            int length = 4 + Integer.parseInt(buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII));
            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }
        } else {
            while (buf.getByte(buf.readerIndex()) == '\r' || buf.getByte(buf.readerIndex()) == '\n') {
                buf.skipBytes(1);
            }
            int delimiter = BufferUtil.indexOf("\r\n", buf);
            if (delimiter == -1) {
                delimiter = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '!');
            }
            if (delimiter != -1) {
                ByteBuf result = buf.readRetainedSlice(delimiter - buf.readerIndex());
                buf.skipBytes(1);
                return result;
            }
        }

        return null;
    }

}
