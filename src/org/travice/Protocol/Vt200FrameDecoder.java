
package org.travice.protocol;

import org.travice.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class Vt200FrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) ')') + 1;
        if (endIndex > 0) {

            ByteBuf frame = Unpooled.buffer();

            while (buf.readerIndex() < endIndex) {
                int b = buf.readByte();
                if (b == '=') {
                    frame.writeByte(buf.readByte() ^ '=');
                } else {
                    frame.writeByte(b);
                }
            }

            return frame;

        }

        return null;
    }

}
