
package org.travice.protocol;

import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.charset.StandardCharsets;

public class AtrackProtocolEncoder extends BaseProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return Unpooled.copiedBuffer(
                        command.getString(Command.KEY_DATA) + "\r\n", StandardCharsets.US_ASCII);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
