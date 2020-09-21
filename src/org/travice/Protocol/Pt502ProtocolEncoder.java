
package org.travice.protocol;

import java.util.TimeZone;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class Pt502ProtocolEncoder extends StringProtocolEncoder implements StringProtocolEncoder.ValueFormatter {

    @Override
    public String formatValue(String key, Object value) {
        if (key.equals(Command.KEY_TIMEZONE)) {
            return String.valueOf(TimeZone.getTimeZone((String) value).getRawOffset() / 3600000);
        }

        return null;
    }

    @Override
    protected String formatCommand(Command command, String format, String... keys) {
        return formatCommand(command, format, this, keys);
    }

    @Override
    protected Object encodeCommand(Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(command, "{%s}\r\n", Command.KEY_DATA);
            case Command.TYPE_OUTPUT_CONTROL:
                return formatCommand(command, "#OPC{%s},{%s}\r\n", Command.KEY_INDEX, Command.KEY_DATA);
            case Command.TYPE_SET_TIMEZONE:
                return formatCommand(command, "#TMZ{%s}\r\n", Command.KEY_TIMEZONE);
            case Command.TYPE_ALARM_SPEED:
                return formatCommand(command, "#SPD{%s}\r\n", Command.KEY_DATA);
            case Command.TYPE_REQUEST_PHOTO:
                return formatCommand(command, "#PHO\r\n");
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
