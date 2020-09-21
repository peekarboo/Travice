
package org.travice.protocol;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class GranitProtocolEncoder extends BaseProtocolEncoder {
    @Override
    protected Object encodeCommand(Command command) {

        String commandString = "";

        switch (command.getType()) {
            case Command.TYPE_IDENTIFICATION:
                commandString = "BB+IDNT";
                break;
            case Command.TYPE_REBOOT_DEVICE:
                commandString = "BB+RESET";
                break;
            case Command.TYPE_POSITION_SINGLE:
                commandString = "BB+RRCD";
                break;
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                return null;
        }
        if (!commandString.isEmpty()) {
            ByteBuf commandBuf = Unpooled.buffer();
            commandBuf.writeBytes(commandString.getBytes(StandardCharsets.US_ASCII));
            GranitProtocolDecoder.appendChecksum(commandBuf, commandString.length());
            return commandBuf;
        }
        return null;
    }

}
