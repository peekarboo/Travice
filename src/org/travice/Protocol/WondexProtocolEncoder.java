
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class WondexProtocolEncoder extends StringProtocolEncoder {
    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "0000");

        switch (command.getType()) {
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(command, "$WP+REBOOT={%s}", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_DEVICE_STATUS:
                return formatCommand(command, "$WP+TEST={%s}", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_MODEM_STATUS:
                return formatCommand(command, "$WP+GSMINFO={%s}", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_IDENTIFICATION:
                return formatCommand(command, "$WP+IMEI={%s}", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(command, "$WP+GETLOCATION={%s}", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_GET_VERSION:
                return formatCommand(command, "$WP+VER={%s}", Command.KEY_DEVICE_PASSWORD);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
