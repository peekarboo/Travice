
package org.travice.protocol;

import org.travice.BaseFrameDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Gl200FrameDecoder extends BaseFrameDecoder {

    private static final int MINIMUM_LENGTH = 11;

    private static final Set<String> BINARY_HEADERS = new HashSet<>(
            Arrays.asList("+RSP", "+BSP", "+EVT", "+BVT", "+INF", "+BNF", "+HBD", "+CRD", "+BRD"));

    public static boolean isBinary(ByteBuf buf) {
        String header = buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII);
        if (header.equals("+ACK")) {
            return buf.getByte(buf.readerIndex() + header.length()) != (byte) ':';
        } else {
            return BINARY_HEADERS.contains(header);
        }
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < MINIMUM_LENGTH) {
            return null;
        }

        if (isBinary(buf)) {

            int length;
            switch (buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII)) {
                case "+ACK":
                    length = buf.getUnsignedByte(buf.readerIndex() + 6);
                    break;
                case "+INF":
                case "+BNF":
                    length = buf.getUnsignedShort(buf.readerIndex() + 7);
                    break;
                case "+HBD":
                    length = buf.getUnsignedByte(buf.readerIndex() + 5);
                    break;
                case "+CRD":
                case "+BRD":
                    length = buf.getUnsignedShort(buf.readerIndex() + 6);
                    break;
                default:
                    length = buf.getUnsignedShort(buf.readerIndex() + 9);
                    break;
            }

            if (buf.readableBytes() >= length) {
                return buf.readRetainedSlice(length);
            }

        } else {

            int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '$');
            if (endIndex < 0) {
                endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) 0);
            }
            if (endIndex > 0) {
                ByteBuf frame = buf.readRetainedSlice(endIndex - buf.readerIndex());
                buf.readByte(); // delimiter
                return frame;
            }

        }

        return null;
    }

}
