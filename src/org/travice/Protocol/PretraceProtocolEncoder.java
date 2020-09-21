
package org.travice.protocol;

import org.travice.BaseProtocolEncoder;
import org.travice.Context;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Command;

public class PretraceProtocolEncoder extends BaseProtocolEncoder {

    private String formatCommand(String uniqueId, String data) {
        String content = uniqueId + data;
        return String.format("(%s^%02X)", content, Checksum.xor(content));
    }

    @Override
    protected Object encodeCommand(Command command) {

        String uniqueId = Context.getIdentityManager().getById(command.getDeviceId()).getUniqueId();

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(uniqueId, command.getString(Command.KEY_DATA));
            case Command.TYPE_POSITION_PERIODIC:
                return formatCommand(
                        uniqueId, String.format("D221%1$d,%1$d,,", command.getInteger(Command.KEY_FREQUENCY)));
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                return null;
        }
    }

}
