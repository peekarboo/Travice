
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.DeviceState;
import org.travice.model.Event;
import org.travice.model.Geofence;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class OverspeedEventHandler extends BaseEventHandler {

    public static final String ATTRIBUTE_SPEED_LIMIT = "speedLimit";

    private boolean notRepeat;
    private boolean preferLowest;
    private long minimalDuration;

    public OverspeedEventHandler(long minimalDuration, boolean notRepeat, boolean preferLowest) {
        this.notRepeat = notRepeat;
        this.minimalDuration = minimalDuration;
        this.preferLowest = preferLowest;
    }

    private Map<Event, Position> newEvent(DeviceState deviceState, double speedLimit) {
        Position position = deviceState.getOverspeedPosition();
        Event event = new Event(Event.TYPE_DEVICE_OVERSPEED, position.getDeviceId(), position.getId());
        event.set("speed", deviceState.getOverspeedPosition().getSpeed());
        event.set(ATTRIBUTE_SPEED_LIMIT, speedLimit);
        event.setGeofenceId(deviceState.getOverspeedGeofenceId());
        deviceState.setOverspeedState(notRepeat);
        deviceState.setOverspeedPosition(null);
        deviceState.setOverspeedGeofenceId(0);
        return Collections.singletonMap(event, position);
    }

    public Map<Event, Position> updateOverspeedState(DeviceState deviceState, double speedLimit) {
        Map<Event, Position> result = null;
        if (deviceState.getOverspeedState() != null && !deviceState.getOverspeedState()
                && deviceState.getOverspeedPosition() != null && speedLimit != 0) {
            long currentTime = System.currentTimeMillis();
            Position overspeedPosition = deviceState.getOverspeedPosition();
            long overspeedTime = overspeedPosition.getFixTime().getTime();
            if (overspeedTime + minimalDuration <= currentTime) {
                result = newEvent(deviceState, speedLimit);
            }
        }
        return result;
    }

    public Map<Event, Position> updateOverspeedState(
            DeviceState deviceState, Position position, double speedLimit, long geofenceId) {
        Map<Event, Position> result = null;

        Boolean oldOverspeed = deviceState.getOverspeedState();

        long currentTime = position.getFixTime().getTime();
        boolean newOverspeed = position.getSpeed() > speedLimit;
        if (newOverspeed && !oldOverspeed) {
            if (deviceState.getOverspeedPosition() == null) {
                deviceState.setOverspeedPosition(position);
                deviceState.setOverspeedGeofenceId(geofenceId);
            }
        } else if (oldOverspeed && !newOverspeed) {
            deviceState.setOverspeedState(false);
            deviceState.setOverspeedPosition(null);
            deviceState.setOverspeedGeofenceId(0);
        } else {
            deviceState.setOverspeedPosition(null);
            deviceState.setOverspeedGeofenceId(0);
        }
        Position overspeedPosition = deviceState.getOverspeedPosition();
        if (overspeedPosition != null) {
            long overspeedTime = overspeedPosition.getFixTime().getTime();
            if (newOverspeed && overspeedTime + minimalDuration <= currentTime) {
                result = newEvent(deviceState, speedLimit);
            }
        }
        return result;
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {

        long deviceId = position.getDeviceId();
        Device device = Context.getIdentityManager().getById(deviceId);
        if (device == null) {
            return null;
        }
        if (!Context.getIdentityManager().isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        double speedLimit = Context.getDeviceManager().lookupAttributeDouble(deviceId, ATTRIBUTE_SPEED_LIMIT, 0, false);

        double geofenceSpeedLimit = 0;
        long overspeedGeofenceId = 0;

        if (Context.getGeofenceManager() != null && device.getGeofenceIds() != null) {
            for (long geofenceId : device.getGeofenceIds()) {
                Geofence geofence = Context.getGeofenceManager().getById(geofenceId);
                if (geofence != null) {
                    double currentSpeedLimit = geofence.getDouble(ATTRIBUTE_SPEED_LIMIT);
                    if (currentSpeedLimit > 0 && geofenceSpeedLimit == 0
                            || preferLowest && currentSpeedLimit < geofenceSpeedLimit
                            || !preferLowest && currentSpeedLimit > geofenceSpeedLimit) {
                        geofenceSpeedLimit = currentSpeedLimit;
                        overspeedGeofenceId = geofenceId;
                    }
                }
            }
        }
        if (geofenceSpeedLimit > 0) {
            speedLimit = geofenceSpeedLimit;
        }

        if (speedLimit == 0) {
            return null;
        }

        Map<Event, Position> result = null;
        DeviceState deviceState = Context.getDeviceManager().getDeviceState(deviceId);

        if (deviceState.getOverspeedState() == null) {
            deviceState.setOverspeedState(position.getSpeed() > speedLimit);
            deviceState.setOverspeedGeofenceId(position.getSpeed() > speedLimit ? overspeedGeofenceId : 0);
        } else {
            result = updateOverspeedState(deviceState, position, speedLimit, overspeedGeofenceId);
        }

        Context.getDeviceManager().setDeviceState(deviceId, deviceState);
        return result;
    }

}
