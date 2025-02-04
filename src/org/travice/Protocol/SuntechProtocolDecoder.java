
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.Context;
import org.travice.DeviceSession;
import org.travice.helper.BitUtil;
import org.travice.helper.UnitsConverter;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SuntechProtocolDecoder extends BaseProtocolDecoder {

    private int protocolType;
    private boolean hbm;
    private boolean includeAdc;
    private boolean includeTemp;

    public SuntechProtocolDecoder(SuntechProtocol protocol) {
        super(protocol);

        protocolType = Context.getConfig().getInteger(getProtocolName() + ".protocolType");
        hbm = Context.getConfig().getBoolean(getProtocolName() + ".hbm");
        includeAdc = Context.getConfig().getBoolean(getProtocolName() + ".includeAdc");
        includeTemp = Context.getConfig().getBoolean(getProtocolName() + ".includeTemp");
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public void setHbm(boolean hbm) {
        this.hbm = hbm;
    }

    public void setIncludeAdc(boolean includeAdc) {
        this.includeAdc = includeAdc;
    }

    public void setIncludeTemp(boolean includeTemp) {
        this.includeTemp = includeTemp;
    }

    private Position decode9(
            Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
        int index = 1;

        String type = values[index++];

        if (!type.equals("Location") && !type.equals("Emergency") && !type.equals("Alert")) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[index++]);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        if (type.equals("Emergency") || type.equals("Alert")) {
            position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
        }

        if (!type.equals("Alert") || protocolType == 0) {
            position.set(Position.KEY_VERSION_FW, values[index++]);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        position.setTime(dateFormat.parse(values[index++] + values[index++]));

        if (protocolType == 1) {
            index += 1; // cell
        }

        position.setLatitude(Double.parseDouble(values[index++]));
        position.setLongitude(Double.parseDouble(values[index++]));
        position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
        position.setCourse(Double.parseDouble(values[index++]));

        position.setValid(values[index++].equals("1"));

        if (protocolType == 1) {
            position.set(Position.KEY_ODOMETER, Integer.parseInt(values[index++]));
        }

        return position;
    }

    private String decodeEmergency(int value) {
        switch (value) {
            case 1:
                return Position.ALARM_SOS;
            case 2:
                return Position.ALARM_PARKING;
            case 3:
                return Position.ALARM_POWER_CUT;
            case 5:
            case 6:
                return Position.ALARM_DOOR;
            case 7:
                return Position.ALARM_MOVEMENT;
            case 8:
                return Position.ALARM_SHOCK;
            default:
                return null;
        }
    }

    private String decodeAlert(int value) {
        switch (value) {
            case 1:
                return Position.ALARM_OVERSPEED;
            case 5:
                return Position.ALARM_GEOFENCE_EXIT;
            case 6:
                return Position.ALARM_GEOFENCE_ENTER;
            case 14:
                return Position.ALARM_LOW_BATTERY;
            case 15:
                return Position.ALARM_SHOCK;
            case 16:
                return Position.ALARM_ACCIDENT;
            case 46:
                return Position.ALARM_ACCELERATION;
            case 47:
                return Position.ALARM_BRAKING;
            case 48:
                return Position.ALARM_ACCIDENT;
            case 50:
                return Position.ALARM_JAMMING;
            default:
                return null;
        }
    }

    private Position decode2356(
            Channel channel, SocketAddress remoteAddress, String protocol, String[] values) throws ParseException {
        int index = 0;

        String type = values[index++].substring(5);

        if (!type.equals("STT") && !type.equals("EMG") && !type.equals("EVT") && !type.equals("ALT")) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[index++]);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.set(Position.KEY_TYPE, type);

        if (protocol.equals("ST300") || protocol.equals("ST500") || protocol.equals("ST600")) {
            index += 1; // model
        }

        position.set(Position.KEY_VERSION_FW, values[index++]);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        position.setTime(dateFormat.parse(values[index++] + values[index++]));

        if (!protocol.equals("ST500")) {
            int cid = Integer.parseInt(values[index++], 16);
            if (protocol.equals("ST600")) {
                position.setNetwork(new Network(CellTower.from(
                        Integer.parseInt(values[index++]), Integer.parseInt(values[index++]),
                        Integer.parseInt(values[index++], 16), cid, Integer.parseInt(values[index++]))));
            }
        }

        position.setLatitude(Double.parseDouble(values[index++]));
        position.setLongitude(Double.parseDouble(values[index++]));
        position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
        position.setCourse(Double.parseDouble(values[index++]));

        position.set(Position.KEY_SATELLITES, Integer.parseInt(values[index++]));

        position.setValid(values[index++].equals("1"));

        position.set(Position.KEY_ODOMETER, Integer.parseInt(values[index++]));
        position.set(Position.KEY_POWER, Double.parseDouble(values[index++]));

        String io = values[index++];
        if (io.length() == 6) {
            position.set(Position.KEY_IGNITION, io.charAt(0) == '1');
            position.set(Position.PREFIX_IN + 1, io.charAt(1) == '1');
            position.set(Position.PREFIX_IN + 2, io.charAt(2) == '1');
            position.set(Position.PREFIX_IN + 3, io.charAt(3) == '1');
            position.set(Position.PREFIX_OUT + 1, io.charAt(4) == '1');
            position.set(Position.PREFIX_OUT + 2, io.charAt(5) == '1');
        }

        switch (type) {
            case "STT":
                index += 1; // mode
                position.set(Position.KEY_INDEX, Integer.parseInt(values[index++]));
                break;
            case "EMG":
                position.set(Position.KEY_ALARM, decodeEmergency(Integer.parseInt(values[index++])));
                break;
            case "EVT":
                position.set(Position.KEY_EVENT, Integer.parseInt(values[index++]));
                break;
            case "ALT":
                position.set(Position.KEY_ALARM, decodeAlert(Integer.parseInt(values[index++])));
                break;
            default:
                break;
        }

        if (hbm) {

            if (index < values.length) {
                position.set(Position.KEY_HOURS, UnitsConverter.msFromMinutes(Integer.parseInt(values[index++])));
            }

            if (index < values.length) {
                position.set(Position.KEY_BATTERY, Double.parseDouble(values[index++]));
            }

            if (index < values.length && values[index++].equals("0")) {
                position.set(Position.KEY_ARCHIVE, true);
            }

            if (includeAdc) {
                position.set(Position.PREFIX_ADC + 1, Double.parseDouble(values[index++]));
                position.set(Position.PREFIX_ADC + 2, Double.parseDouble(values[index++]));
                position.set(Position.PREFIX_ADC + 3, Double.parseDouble(values[index++]));
            }

            if (values.length - index >= 2) {
                String driverUniqueId = values[index++];
                if (values[index++].equals("1") && !driverUniqueId.isEmpty()) {
                    position.set(Position.KEY_DRIVER_UNIQUE_ID, driverUniqueId);
                }
            }

            if (includeTemp) {
                for (int i = 1; i <= 3; i++) {
                    String temperature = values[index++];
                    String value = temperature.substring(temperature.indexOf(':') + 1);
                    if (!value.isEmpty()) {
                        position.set(Position.PREFIX_TEMP + i, Double.parseDouble(value));
                    }
                }

            }

        }

        return position;
    }

    private Position decodeUniversal(
            Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
        int index = 0;

        String type = values[index++];

        if (!type.equals("STT")) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[index++]);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.set(Position.KEY_TYPE, type);

        int mask = Integer.parseInt(values[index++], 16);

        if (BitUtil.check(mask, 1)) {
            index += 1; // model
        }

        if (BitUtil.check(mask, 2)) {
            position.set(Position.KEY_VERSION_FW, values[index++]);
        }

        if (BitUtil.check(mask, 3) && values[index++].equals("0")) {
            position.set(Position.KEY_ARCHIVE, true);
        }

        if (BitUtil.check(mask, 4) && BitUtil.check(mask, 5)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            position.setTime(dateFormat.parse(values[index++] + values[index++]));
        }

        if (BitUtil.check(mask, 6)) {
            index += 1; // cell
        }

        if (BitUtil.check(mask, 7)) {
            index += 1; // mcc
        }

        if (BitUtil.check(mask, 8)) {
            index += 1; // mnc
        }

        if (BitUtil.check(mask, 9)) {
            index += 1; // lac
        }

        if (BitUtil.check(mask, 10)) {
            position.set(Position.KEY_RSSI, Integer.parseInt(values[index++]));
        }

        if (BitUtil.check(mask, 11)) {
            position.setLatitude(Double.parseDouble(values[index++]));
        }

        if (BitUtil.check(mask, 12)) {
            position.setLongitude(Double.parseDouble(values[index++]));
        }

        if (BitUtil.check(mask, 13)) {
            position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
        }

        if (BitUtil.check(mask, 14)) {
            position.setCourse(Double.parseDouble(values[index++]));
        }

        if (BitUtil.check(mask, 15)) {
            position.set(Position.KEY_SATELLITES, Integer.parseInt(values[index++]));
        }

        if (BitUtil.check(mask, 16)) {
            position.setValid(values[index++].equals("1"));
        }

        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String[] values = ((String) msg).split(";");

        if (values[0].length() < 5) {
            return decodeUniversal(channel, remoteAddress, values);
        } else if (values[0].equals("ST910")) {
            return decode9(channel, remoteAddress, values);
        } else {
            return decode2356(channel, remoteAddress, values[0].substring(0, 5), values);
        }
    }

}
