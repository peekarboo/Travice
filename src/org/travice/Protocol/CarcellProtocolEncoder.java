
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class CarcellProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(command, "$SRVCMD,{%s},BA#\r\n", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(command, "$SRVCMD,{%s},BD#\r\n", Command.KEY_UNIQUE_ID);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
