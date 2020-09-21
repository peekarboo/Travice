
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;
import org.travice.helper.BufferUtil;

public class AtrackFrameDecoder extends BaseFrameDecoder {

    private static final int KEEPALIVE_LENGTH = 12;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() >= 2) {

            if (buf.getUnsignedShort(buf.readerIndex()) == 0xfe02) {

                if (buf.readableBytes() >= KEEPALIVE_LENGTH) {
                    return buf.readRetainedSlice(KEEPALIVE_LENGTH);
                }

            } else if (buf.getUnsignedShort(buf.readerIndex()) == 0x4050 && buf.getByte(buf.readerIndex() + 2) != ',') {

                if (buf.readableBytes() > 6) {
                    int length = buf.getUnsignedShort(buf.readerIndex() + 4) + 4 + 2;
                    if (buf.readableBytes() >= length) {
                        return buf.readRetainedSlice(length);
                    }
                }

            } else {

                int endIndex = BufferUtil.indexOf("\r\n", buf);
                if (endIndex > 0) {
                    return buf.readRetainedSlice(endIndex - buf.readerIndex() + 2);
                }

            }

        }

        return null;
    }

}
