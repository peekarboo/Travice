
package org.travice.helper;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public final class BufferUtil {

    private BufferUtil() {
    }

    public static int indexOf(String needle, ByteBuf haystack) {
        ByteBuf needleBuffer = Unpooled.wrappedBuffer(needle.getBytes(StandardCharsets.US_ASCII));
        try {
            return ByteBufUtil.indexOf(needleBuffer, haystack);
        } finally {
            needleBuffer.release();
        }
    }

    public static int indexOf(String needle, ByteBuf haystack, int startIndex, int endIndex) {
        haystack = Unpooled.wrappedBuffer(haystack);
        haystack.readerIndex(startIndex);
        haystack.writerIndex(endIndex);
        return indexOf(needle, haystack);
    }

}
