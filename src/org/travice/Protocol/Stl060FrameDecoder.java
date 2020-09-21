
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.travice.CharacterDelimiterFrameDecoder;

public class Stl060FrameDecoder extends CharacterDelimiterFrameDecoder {

    public Stl060FrameDecoder(int maxFrameLength) {
        super(maxFrameLength, '#');
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {

        ByteBuf result = (ByteBuf) super.decode(ctx, buf);

        if (result != null) {

            int index = result.indexOf(result.readerIndex(), result.writerIndex(), (byte) '$');
            if (index == -1) {
                return result;
            } else {
                result.skipBytes(index);
                return result.readRetainedSlice(result.readableBytes());
            }

        }

        return null;
    }

}
