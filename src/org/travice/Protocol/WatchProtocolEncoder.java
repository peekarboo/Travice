
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.StringProtocolEncoder;
import org.travice.helper.DataConverter;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WatchProtocolEncoder extends StringProtocolEncoder implements StringProtocolEncoder.ValueFormatter {

    @Override
    public String formatValue(String key, Object value) {
        if (key.equals(Command.KEY_TIMEZONE)) {
            double offset = TimeZone.getTimeZone((String) value).getRawOffset() / 3600000.0;
            DecimalFormat fmt = new DecimalFormat("+#.##;-#.##", DecimalFormatSymbols.getInstance(Locale.US));
            return fmt.format(offset);
        }

        return null;
    }

    protected String formatCommand(Channel channel, Command command, String format, String... keys) {

        boolean hasIndex = false;
        String manufacturer = "CS";
        if (channel != null) {
            WatchProtocolDecoder decoder = channel.pipeline().get(WatchProtocolDecoder.class);
            if (decoder != null) {
                hasIndex = decoder.getHasIndex();
                manufacturer = decoder.getManufacturer();
            }
        }

        String content = formatCommand(command, format, this, keys);

        if (hasIndex) {
            return String.format("[%s*%s*0001*%04x*%s]",
                    manufacturer, getUniqueId(command.getDeviceId()), content.length(), content);
        } else {
            return String.format("[%s*%s*%04x*%s]",
                    manufacturer, getUniqueId(command.getDeviceId()), content.length(), content);
        }
    }

    private int getEnableFlag(Command command) {
        if (command.getBoolean(Command.KEY_ENABLE)) {
            return 1;
        } else {
            return 0;
        }
    }

    private static Map<Byte, Byte> mapping = new HashMap<>();

    static {
        mapping.put((byte) 0x7d, (byte) 0x01);
        mapping.put((byte) 0x5B, (byte) 0x02);
        mapping.put((byte) 0x5D, (byte) 0x03);
        mapping.put((byte) 0x2C, (byte) 0x04);
        mapping.put((byte) 0x2A, (byte) 0x05);
    }

    private String getBinaryData(Command command) {
        byte[] data = DataConverter.parseHex(command.getString(Command.KEY_DATA));

        int encodedLength = data.length;
        for (byte b : data) {
            if (mapping.containsKey(b)) {
                encodedLength += 1;
            }
        }

        int index = 0;
        byte[] encodedData = new byte[encodedLength];

        for (byte b : data) {
            Byte replacement = mapping.get(b);
            if (replacement != null) {
                encodedData[index] = 0x7D;
                index += 1;
                encodedData[index] = replacement;
            } else {
                encodedData[index] = b;
            }
            index += 1;
        }

        return new String(encodedData, StandardCharsets.US_ASCII);
    }

    @Override
    protected Object encodeCommand(Channel channel, Command command) {

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                return formatCommand(channel, command, command.getString(Command.KEY_DATA));
            case Command.TYPE_POSITION_SINGLE:
                return formatCommand(channel, command, "RG");
            case Command.TYPE_SOS_NUMBER:
                return formatCommand(channel, command, "SOS{%s},{%s}", Command.KEY_INDEX, Command.KEY_PHONE);
            case Command.TYPE_ALARM_SOS:
                return formatCommand(channel, command, "SOSSMS," + getEnableFlag(command));
            case Command.TYPE_ALARM_BATTERY:
                return formatCommand(channel, command, "LOWBAT," + getEnableFlag(command));
            case Command.TYPE_REBOOT_DEVICE:
                return formatCommand(channel, command, "RESET");
            case Command.TYPE_ALARM_REMOVE:
                return formatCommand(channel, command, "REMOVE," + getEnableFlag(command));
            case Command.TYPE_SILENCE_TIME:
                return formatCommand(channel, command, "SILENCETIME,{%s}", Command.KEY_DATA);
            case Command.TYPE_ALARM_CLOCK:
                return formatCommand(channel, command, "REMIND,{%s}", Command.KEY_DATA);
            case Command.TYPE_SET_PHONEBOOK:
                return formatCommand(channel, command, "PHB,{%s}", Command.KEY_DATA);
            case Command.TYPE_VOICE_MESSAGE:
                return formatCommand(channel, command, "TK," + getBinaryData(command));
            case Command.TYPE_POSITION_PERIODIC:
                return formatCommand(channel, command, "UPLOAD,{%s}", Command.KEY_FREQUENCY);
            case Command.TYPE_SET_TIMEZONE:
                return formatCommand(channel, command, "LZ,,{%s}", Command.KEY_TIMEZONE);
            case Command.TYPE_SET_INDICATOR:
                return formatCommand(channel, command, "FLOWER,{%s}", Command.KEY_DATA);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
