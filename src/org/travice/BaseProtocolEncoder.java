
package org.travice;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.travice.helper.Log;
import org.travice.model.Command;
import org.travice.model.Device;

public abstract class BaseProtocolEncoder extends ChannelOutboundHandlerAdapter {

    protected String getUniqueId(long deviceId) {
        return Context.getIdentityManager().getById(deviceId).getUniqueId();
    }

    protected void initDevicePassword(Command command, String defaultPassword) {
        if (!command.getAttributes().containsKey(Command.KEY_DEVICE_PASSWORD)) {
            Device device = Context.getIdentityManager().getById(command.getDeviceId());
            String password = device.getString(Command.KEY_DEVICE_PASSWORD);
            if (password != null) {
                command.set(Command.KEY_DEVICE_PASSWORD, password);
            } else {
                command.set(Command.KEY_DEVICE_PASSWORD, defaultPassword);
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        NetworkMessage networkMessage = (NetworkMessage) msg;

        if (networkMessage.getMessage() instanceof Command) {

            Command command = (Command) networkMessage.getMessage();
            Object encodedCommand = encodeCommand(ctx.channel(), command);

            StringBuilder s = new StringBuilder();
            s.append("[").append(ctx.channel().id().asShortText()).append("] ");
            s.append("id: ").append(getUniqueId(command.getDeviceId())).append(", ");
            s.append("command type: ").append(command.getType()).append(" ");
            if (encodedCommand != null) {
                s.append("sent");
            } else {
                s.append("not sent");
            }
            Log.info(s.toString());

            ctx.write(new NetworkMessage(encodedCommand, networkMessage.getRemoteAddress()), promise);

        } else {

            super.write(ctx, msg, promise);

        }
    }

    protected Object encodeCommand(Channel channel, Command command) {
        return encodeCommand(command);
    }

    protected Object encodeCommand(Command command) {
        return null;
    }

}
