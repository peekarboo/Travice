
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;
import org.travice.helper.Log;

public class CellocatorFrameDecoder extends BaseFrameDecoder {

    private static final int MESSAGE_MINIMUM_LENGTH = 15;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        int length = 0;
        int type = buf.getUnsignedByte(4);
        switch (type) {
            case CellocatorProtocolDecoder.MSG_CLIENT_STATUS:
                length = 70;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_PROGRAMMING:
                length = 31;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_SERIAL_LOG:
                length = 70;
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_SERIAL:
                if (buf.readableBytes() >= 19) {
                    length = 19 + buf.getUnsignedShortLE(16);
                }
                break;
            case CellocatorProtocolDecoder.MSG_CLIENT_MODULAR:
                length = 15 + buf.getUnsignedByte(13);
                break;
            default:
                Log.warning(new UnsupportedOperationException(String.valueOf(type)));
                break;
        }

        if (length > 0 && buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
