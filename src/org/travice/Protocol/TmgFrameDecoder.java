
package org.travice.protocol;

import org.travice.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TmgFrameDecoder extends BaseFrameDecoder {

    private boolean isLetter(byte c) {
        return c >= 'a' && c <= 'z';
    }

    private int findHeader(ByteBuf buffer) {
        int guessedIndex = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), (byte) '$');
        while (guessedIndex != -1 && buffer.writerIndex() - guessedIndex >= 5) {
            if (buffer.getByte(guessedIndex + 4) == ','
                        && isLetter(buffer.getByte(guessedIndex + 1))
                        && isLetter(buffer.getByte(guessedIndex + 2))
                        && isLetter(buffer.getByte(guessedIndex + 3))) {
                return guessedIndex;
            }
            guessedIndex = buffer.indexOf(guessedIndex, buffer.writerIndex(), (byte) '$');
        }
        return -1;
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int beginIndex = findHeader(buf);

        if (beginIndex >= 0) {

            buf.readerIndex(beginIndex);

            int endIndex = buf.indexOf(beginIndex, buf.writerIndex(), (byte) '\n');

            if (endIndex >= 0) {
                ByteBuf frame = buf.readRetainedSlice(endIndex - beginIndex);
                buf.readByte(); // delimiter
                return frame;
            }

        }

        return null;
    }

}
