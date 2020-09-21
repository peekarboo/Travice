
package org.travice.model;

import java.util.Date;

public class Event extends Message {

    public Event(String type, long deviceId, long positionId) {
        this(type, deviceId);
        setPositionId(positionId);
    }

    public Event(String type, long deviceId) {
        setType(type);
        setDeviceId(deviceId);
        this.serverTime = new Date();
    }

    public Event() {
    }

    public static final String ALL_EVENTS = "allEvents";

    public static final String TYPE_COMMAND_RESULT = "commandResult";

    public static final String TYPE_DEVICE_ONLINE = "deviceOnline";
    public static final String TYPE_DEVICE_UNKNOWN = "deviceUnknown";
    public static final String TYPE_DEVICE_OFFLINE = "deviceOffline";

    public static final String TYPE_DEVICE_MOVING = "deviceMoving";
    public static final String TYPE_DEVICE_STOPPED = "deviceStopped";

    public static final String TYPE_DEVICE_OVERSPEED = "deviceOverspeed";
    public static final String TYPE_DEVICE_FUEL_DROP = "deviceFuelDrop";

    public static final String TYPE_GEOFENCE_ENTER = "geofenceEnter";
    public static final String TYPE_GEOFENCE_EXIT = "geofenceExit";

    public static final String TYPE_ALARM = "alarm";

    public static final String TYPE_IGNITION_ON = "ignitionOn";
    public static final String TYPE_IGNITION_OFF = "ignitionOff";

    public static final String TYPE_MAINTENANCE = "maintenance";

    public static final String TYPE_TEXT_MESSAGE = "textMessage";

    public static final String TYPE_DRIVER_CHANGED = "driverChanged";

    private Date serverTime;

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    private long positionId;

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    private long geofenceId = 0;

    public long getGeofenceId() {
        return geofenceId;
    }

    public void setGeofenceId(long geofenceId) {
        this.geofenceId = geofenceId;
    }

    private long maintenanceId = 0;

    public long getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(long maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

}
