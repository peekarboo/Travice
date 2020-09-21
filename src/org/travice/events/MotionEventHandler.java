
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.DeviceState;
import org.travice.model.Event;
import org.travice.model.Position;
import org.travice.reports.ReportUtils;
import org.travice.reports.model.TripsConfig;

@ChannelHandler.Sharable
public class MotionEventHandler extends BaseEventHandler {

    private TripsConfig tripsConfig;

    public MotionEventHandler(TripsConfig tripsConfig) {
        this.tripsConfig = tripsConfig;
    }

    private Map<Event, Position> newEvent(DeviceState deviceState, boolean newMotion) {
        String eventType = newMotion ? Event.TYPE_DEVICE_MOVING : Event.TYPE_DEVICE_STOPPED;
        Position position = deviceState.getMotionPosition();
        Event event = new Event(eventType, position.getDeviceId(), position.getId());
        deviceState.setMotionState(newMotion);
        deviceState.setMotionPosition(null);
        return Collections.singletonMap(event, position);
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState) {
        Map<Event, Position> result = null;
        if (deviceState.getMotionState() != null && deviceState.getMotionPosition() != null) {
            boolean newMotion = !deviceState.getMotionState();
            Position motionPosition = deviceState.getMotionPosition();
            long currentTime = System.currentTimeMillis();
            long motionTime = motionPosition.getFixTime().getTime()
                    + (newMotion ? tripsConfig.getMinimalTripDuration() : tripsConfig.getMinimalParkingDuration());
            if (motionTime <= currentTime) {
                result = newEvent(deviceState, newMotion);
            }
        }
        return result;
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState, Position position) {
        return updateMotionState(deviceState, position, position.getBoolean(Position.KEY_MOTION));
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState, Position position, boolean newMotion) {
        Map<Event, Position> result = null;
        Boolean oldMotion = deviceState.getMotionState();

        long currentTime = position.getFixTime().getTime();
        if (newMotion != oldMotion) {
            if (deviceState.getMotionPosition() == null) {
                deviceState.setMotionPosition(position);
            }
        } else {
            deviceState.setMotionPosition(null);
        }

        Position motionPosition = deviceState.getMotionPosition();
        if (motionPosition != null) {
            long motionTime = motionPosition.getFixTime().getTime();
            double distance = ReportUtils.calculateDistance(motionPosition, position, false);
            Boolean ignition = null;
            if (tripsConfig.getUseIgnition()
                    && position.getAttributes().containsKey(Position.KEY_IGNITION)) {
                ignition = position.getBoolean(Position.KEY_IGNITION);
            }
            if (newMotion) {
                if (motionTime + tripsConfig.getMinimalTripDuration() <= currentTime
                        || distance >= tripsConfig.getMinimalTripDistance()) {
                    result = newEvent(deviceState, newMotion);
                }
            } else {
                if (motionTime + tripsConfig.getMinimalParkingDuration() <= currentTime
                        || ignition != null && !ignition) {
                    result = newEvent(deviceState, newMotion);
                }
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
        if (!Context.getIdentityManager().isLatestPosition(position)
                || !tripsConfig.getProcessInvalidPositions() && !position.getValid()) {
            return null;
        }

        Map<Event, Position> result = null;
        DeviceState deviceState = Context.getDeviceManager().getDeviceState(deviceId);

        if (deviceState.getMotionState() == null) {
            deviceState.setMotionState(position.getBoolean(Position.KEY_MOTION));
        } else {
            result = updateMotionState(deviceState, position);
        }
        Context.getDeviceManager().setDeviceState(deviceId, deviceState);
        return result;
    }

}
