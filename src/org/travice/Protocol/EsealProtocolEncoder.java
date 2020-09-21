
package org.travice.protocol;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class EsealProtocolEncoder extends StringProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(
                        command, "##S,eSeal,{%s},256,3.0.8,{%s},E##", Command.KEY_UNIQUE_ID, Command.KEY_DATA);
            case Command.TYPE_ALARM_ARM:
                return formatCommand(
                        command, "##S,eSeal,{%s},256,3.0.8,RC-Power Control,Power OFF,E##", Command.KEY_UNIQUE_ID);
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(
                        command, "##S,eSeal,{%s},256,3.0.8,RC-Unlock,E##", Command.KEY_UNIQUE_ID);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
