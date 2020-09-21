
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class WialonProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {
        switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "reboot\r\n");
            case Command.TYPE_SEND_USSD:
                return formatCommand(command, "USSD:{%s}\r\n", Command.KEY_PHONE);
            case Command.TYPE_IDENTIFICATION:
                return formatCommand(command, "VER?\r\n");
            case Command.TYPE_OUTPUT_CONTROL:
                return formatCommand(command, "L{%s}={%s}\r\n", Command.KEY_INDEX, Command.KEY_DATA);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }
        return null;
    }
}
