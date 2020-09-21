
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;
import org.travice.helper.BufferUtil;

public class GranitFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int indexEnd = BufferUtil.indexOf("\r\n", buf);
        if (indexEnd != -1) {
            int indexTilde = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '~');
            if (indexTilde != -1 && indexTilde < indexEnd) {
                int length = buf.getUnsignedShortLE(indexTilde + 1);
                indexEnd = BufferUtil.indexOf("\r\n", buf, indexTilde + 2 + length, buf.writerIndex());
                if (indexEnd == -1) {
                    return null;
                }
            }
            ByteBuf frame = buf.readRetainedSlice(indexEnd - buf.readerIndex());
            buf.skipBytes(2);
            return frame;
        }
        return null;
    }

}
