
package org.travice;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.travice.model.Position;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class RemoteAddressHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostAddress = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : null;

        if (msg instanceof Position) {
            Position position = (Position) msg;
            position.set(Position.KEY_IP, hostAddress);
        }

        ctx.fireChannelRead(msg);
    }

}
