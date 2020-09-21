
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.travice.BaseFrameDecoder;

public class RoboTrackFrameDecoder extends BaseFrameDecoder {

    private int messageLength(ByteBuf buf) {
        switch (buf.getUnsignedByte(buf.readerIndex())) {
            case RoboTrackProtocolDecoder.MSG_ID:
                return 69;
            case RoboTrackProtocolDecoder.MSG_ACK:
                return 3;
            case RoboTrackProtocolDecoder.MSG_GPS:
            case RoboTrackProtocolDecoder.MSG_GSM:
            case RoboTrackProtocolDecoder.MSG_IMAGE_START:
                return 24;
            case RoboTrackProtocolDecoder.MSG_IMAGE_DATA:
                return 8 + buf.getUnsignedShortLE(buf.readerIndex() + 1);
            case RoboTrackProtocolDecoder.MSG_IMAGE_END:
                return 6;
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        int length = messageLength(buf);

        if (buf.readableBytes() >= length) {
            return buf.readRetainedSlice(length);
        }

        return null;
    }

}
