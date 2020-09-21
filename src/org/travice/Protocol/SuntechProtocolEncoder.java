
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class SuntechProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "SA200CMD;{%s};02;Reboot\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "SA200GTR;{%s};02;\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_OUTPUT_CONTROL:
                if (command.getAttributes().containsKey(Command.KEY_DATA)) {
                    if (command.getAttributes().get(Command.KEY_DATA).equals("1")) {
                        return formatCommand(command, "SA200CMD;{%s};02;Enable{%s}\r",
                                Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                    } else {
                        return formatCommand(command, "SA200CMD;{%s};02;Disable{%s}\r",
                                Command.KEY_UNIQUE_ID, Command.KEY_INDEX);
                    }
                }
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "SA200CMD;{%s};02;Enable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "SA200CMD;{%s};02;Disable1\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_ARM:
                return formatCommand(command, "SA200CMD;{%s};02;Enable2\r", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(command, "SA200CMD;{%s};02;Disable2\r", Command.KEY_UNIQUE_ID);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
