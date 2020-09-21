
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class MxtFrameDecoder extends BaseFrameDecoder {

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < 2) {
            return null;
        }

        int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte) 0x04);
        if (index != -1) {
            ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());

            while (buf.readerIndex() <= index) {
                int b = buf.readUnsignedByte();
                if (b == 0x10) {
                    result.writeByte(buf.readUnsignedByte() - 0x20);
                } else {
                    result.writeByte(b);
                }
            }

            return result;
        }

        return null;
    }

}
