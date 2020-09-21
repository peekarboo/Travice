
package org.travice;

import org.travice.model.Command;

import java.util.Map;

public abstract class StringProtocolEncoder extends BaseProtocolEncoder {

    public interface ValueFormatter {
        String formatValue(String key, Object value);
    }

    protected String formatCommand(Command command, String format, ValueFormatter valueFormatter, String... keys) {

        String result = String.format(format, (Object[]) keys);

        result = result.replaceAll("\\{" + Command.KEY_UNIQUE_ID + "}", getUniqueId(command.getDeviceId()));
        for (Map.Entry<String, Object> entry : command.getAttributes().entrySet()) {
            String value = null;
            if (valueFormatter != null) {
                value = valueFormatter.formatValue(entry.getKey(), entry.getValue());
            }
            if (value == null) {
                value = entry.getValue().toString();
            }
            result = result.replaceAll("\\{" + entry.getKey() + "}", value);
        }

        return result;
    }

    protected String formatCommand(Command command, String format, String... keys) {
        return formatCommand(command, format, null, keys);
    }

}
