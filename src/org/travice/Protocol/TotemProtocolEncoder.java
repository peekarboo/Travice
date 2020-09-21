
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class TotemProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "000000");

        switch (command.getType()) {
            // Assuming PIN 8 (Output C) is the power wire, like manual says but it can be PIN 5,7,8
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "*{%s},025,C,1#", Command.KEY_DEVICE_PASSWORD);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "*{%s},025,C,0#", Command.KEY_DEVICE_PASSWORD);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
