
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class AdmProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_GET_DEVICE_STATUS:
                return formatCommand(command, "STATUS\r\n");

            case Command.TYPE_CUSTOM:
                return formatCommand(command, "{%s}\r\n", Command.KEY_DATA);

            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
