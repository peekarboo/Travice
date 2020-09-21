
package org.travice.protocol;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.travice.StringProtocolEncoder;
import org.travice.helper.Log;
import org.travice.model.Command;

public class H02ProtocolEncoder extends StringProtocolEncoder {

    private static final String MARKER = "HQ";

    private Object formatCommand(DateTime time, String uniqueId, String type, String... params) {

        StringBuilder result = new StringBuilder(String.format("*%s,%s,%s,%02d%02d%02d",
                MARKER, uniqueId, type, time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute()));

        for (String param : params) {
            result.append(",").append(param);
        }

        result.append("#");

        return result.toString();
    }

    protected Object encodeCommand(Command command, DateTime time) {
        String uniqueId = getUniqueId(command.getDeviceId());

        switch (command.getType()) {
            case Command.TYPE_ALARM_ARM:
                return formatCommand(time, uniqueId, "SCF", "0", "0");
            case Command.TYPE_ALARM_DISARM:
                return formatCommand(time, uniqueId, "SCF", "1", "1");
            case Command.TYPE_ENGINE_STOP:
                return formatCommand(
                        time, uniqueId, "S20", "1", "3", "10", "3", "5", "5", "3", "5", "3", "5", "3", "5");
            case Command.TYPE_ENGINE_RESUME:
                return formatCommand(time, uniqueId, "S20", "0", "0");
            case Command.TYPE_POSITION_PERIODIC:
                return formatCommand(
                        time, uniqueId, "S71", "22", command.getAttributes().get(Command.KEY_FREQUENCY).toString());
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

    @Override
    protected Object encodeCommand(Command command) {
        return encodeCommand(command, new DateTime(DateTimeZone.UTC));
    }

}
