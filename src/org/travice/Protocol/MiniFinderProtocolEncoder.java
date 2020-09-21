
package org.travice.protocol;

import java.util.TimeZone;

import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class MiniFinderProtocolEncoder extends StringProtocolEncoder implements StringProtocolEncoder.ValueFormatter {

    @Override
    public String formatValue(String key, Object value) {
        switch (key) {
            case Command.KEY_ENABLE:
                return (Boolean) value ? "1" : "0";
            case Command.KEY_TIMEZONE:
                return String.format("%+03d", TimeZone.getTimeZone((String) value).getRawOffset() / 3600000);
            case Command.KEY_INDEX:
                switch (((Number) value).intValue()) {
                    case 0:
                        return "A";
                    case 1:
                        return "B";
                    case 2:
                        return "C";
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    @Override
    protected Object encodeCommand(Command command) {

        initDevicePassword(command, "123456");

        switch (command.getType()) {
            case Command.TYPE_SET_TIMEZONE:
                return formatCommand(command, "{%s}L{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_TIMEZONE);
            case Command.TYPE_VOICE_MONITORING:
                return formatCommand(command, "{%s}P{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_ALARM_SPEED:
                return formatCommand(command, "{%s}J1{%s}", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            case Command.TYPE_ALARM_GEOFENCE:
                return formatCommand(command, "{%s}R1{%s}", Command.KEY_DEVICE_PASSWORD, Command.KEY_RADIUS);
            case Command.TYPE_ALARM_VIBRATION:
                return formatCommand(command, "{%s}W1,{%s}", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            case Command.TYPE_SET_AGPS:
                return formatCommand(command, "{%s}AGPS{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_ALARM_FALL:
                return formatCommand(command, "{%s}F{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_MODE_POWER_SAVING:
                return formatCommand(command, "{%s}SP{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_MODE_DEEP_SLEEP:
                return formatCommand(command, "{%s}DS{%s}", this, Command.KEY_DEVICE_PASSWORD, Command.KEY_ENABLE);
            case Command.TYPE_SOS_NUMBER:
                return formatCommand(command, "{%s}{%s}1,{%s}", this,
                        Command.KEY_DEVICE_PASSWORD, Command.KEY_INDEX, Command.KEY_PHONE);
            case Command.TYPE_SET_INDICATOR:
                return formatCommand(command, "{%s}LED{%s}", Command.KEY_DEVICE_PASSWORD, Command.KEY_DATA);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                return null;
        }
    }

}
