
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

import java.text.ParseException;

public class Jt600FrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        char type = (char) buf.getByte(buf.readerIndex());

        if (type == '$') {
            boolean longFormat = buf.getUnsignedByte(buf.readerIndex() + 1) == 0x75;
            int length = buf.getUnsignedShort(buf.readerIndex() + (longFormat ? 8 : 7)) + 10;
            if (length <= buf.readableBytes()) {
                return buf.readRetainedSlice(length);
            }
        } else if (type == '(') {
            int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ')');
            if (endIndex != -1) {
                return buf.readRetainedSlice(endIndex + 1);
            }
        } else {
            throw new ParseException(null, 0); // unknown message
        }

        return null;
    }

}
