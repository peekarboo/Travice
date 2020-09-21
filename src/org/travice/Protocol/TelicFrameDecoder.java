
package org.travice.protocol;

import org.travice.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TelicFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 4) {
            return null;
        }

        long length = buf.getUnsignedIntLE(buf.readerIndex());

        if (length < 1024) {
            if (buf.readableBytes() >= length + 4) {
                buf.readUnsignedIntLE();
                return buf.readRetainedSlice((int) length);
            }
        } else {
            int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0);
            if (endIndex >= 0) {
                ByteBuf frame = buf.readRetainedSlice(endIndex - buf.readerIndex());
                buf.readByte();
                if (frame.readableBytes() > 0) {
                    return frame;
                }
            }
        }

        return null;
    }

}
