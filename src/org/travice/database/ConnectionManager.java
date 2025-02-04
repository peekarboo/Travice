
package org.travice.database;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.travice.Context;
import org.travice.GlobalTimer;
import org.travice.Protocol;
import org.travice.events.OverspeedEventHandler;
import org.travice.helper.Log;
import org.travice.model.Device;
import org.travice.model.DeviceState;
import org.travice.model.Event;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {

    private static final long DEFAULT_TIMEOUT = 600;

    private final long deviceTimeout;
    private final boolean enableStatusEvents;
    private final boolean updateDeviceState;

    private final Map<Long, ActiveDevice> activeDevices = new ConcurrentHashMap<>();
    private final Map<Long, Set<UpdateListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Long, Timeout> timeouts = new ConcurrentHashMap<>();

    public ConnectionManager() {
        deviceTimeout = Context.getConfig().getLong("status.timeout", DEFAULT_TIMEOUT) * 1000;
        enableStatusEvents = Context.getConfig().getBoolean("event.enable");
        updateDeviceState = Context.getConfig().getBoolean("status.updateDeviceState");
    }

    public void addActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
        activeDevices.put(deviceId, new ActiveDevice(deviceId, protocol, channel, remoteAddress));
    }

    public void removeActiveDevice(Channel channel) {
        for (ActiveDevice activeDevice : activeDevices.values()) {
            if (activeDevice.getChannel() == channel) {
                updateDevice(activeDevice.getDeviceId(), Device.STATUS_OFFLINE, null);
                activeDevices.remove(activeDevice.getDeviceId());
                break;
            }
        }
    }

    public ActiveDevice getActiveDevice(long deviceId) {
        return activeDevices.get(deviceId);
    }

    public void updateDevice(final long deviceId, String status, Date time) {
        Device device = Context.getIdentityManager().getById(deviceId);
        if (device == null) {
            return;
        }

        String oldStatus = device.getStatus();
        device.setStatus(status);

        if (enableStatusEvents && !status.equals(oldStatus)) {
            String eventType;
            Map<Event, Position> events = new HashMap<>();
            switch (status) {
                case Device.STATUS_ONLINE:
                    eventType = Event.TYPE_DEVICE_ONLINE;
                    break;
                case Device.STATUS_UNKNOWN:
                    eventType = Event.TYPE_DEVICE_UNKNOWN;
                    if (updateDeviceState) {
                        events.putAll(updateDeviceState(deviceId));
                    }
                    break;
                default:
                    eventType = Event.TYPE_DEVICE_OFFLINE;
                    if (updateDeviceState) {
                        events.putAll(updateDeviceState(deviceId));
                    }
                    break;
            }
            events.put(new Event(eventType, deviceId), null);
            Context.getNotificationManager().updateEvents(events);
        }

        Timeout timeout = timeouts.remove(deviceId);
        if (timeout != null) {
            timeout.cancel();
        }

        if (time != null) {
            device.setLastUpdate(time);
        }

        if (status.equals(Device.STATUS_ONLINE)) {
            timeouts.put(deviceId, GlobalTimer.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) {
                    if (!timeout.isCancelled()) {
                        updateDevice(deviceId, Device.STATUS_UNKNOWN, null);
                    }
                }
            }, deviceTimeout, TimeUnit.MILLISECONDS));
        }

        try {
            Context.getDeviceManager().updateDeviceStatus(device);
        } catch (SQLException error) {
            Log.warning(error);
        }

        updateDevice(device);

        if (status.equals(Device.STATUS_ONLINE) && !oldStatus.equals(Device.STATUS_ONLINE)) {
            Context.getCommandsManager().sendQueuedCommands(getActiveDevice(deviceId));
        }
    }

    public Map<Event, Position> updateDeviceState(long deviceId) {
        DeviceState deviceState = Context.getDeviceManager().getDeviceState(deviceId);
        Map<Event, Position> result = new HashMap<>();

        Map<Event, Position> event = Context.getMotionEventHandler().updateMotionState(deviceState);
        if (event != null) {
            result.putAll(event);
        }

        event = Context.getOverspeedEventHandler().updateOverspeedState(deviceState, Context.getDeviceManager().
                lookupAttributeDouble(deviceId, OverspeedEventHandler.ATTRIBUTE_SPEED_LIMIT, 0, false));
        if (event != null) {
            result.putAll(event);
        }

        return result;
    }

    public synchronized void updateDevice(Device device) {
        for (long userId : Context.getPermissionsManager().getDeviceUsers(device.getId())) {
            if (listeners.containsKey(userId)) {
                for (UpdateListener listener : listeners.get(userId)) {
                    listener.onUpdateDevice(device);
                }
            }
        }
    }

    public synchronized void updatePosition(Position position) {
        long deviceId = position.getDeviceId();

        for (long userId : Context.getPermissionsManager().getDeviceUsers(deviceId)) {
            if (listeners.containsKey(userId)) {
                for (UpdateListener listener : listeners.get(userId)) {
                    listener.onUpdatePosition(position);
                }
            }
        }
    }

    public synchronized void updateEvent(long userId, Event event) {
        if (listeners.containsKey(userId)) {
            for (UpdateListener listener : listeners.get(userId)) {
                listener.onUpdateEvent(event);
            }
        }
    }

    public interface UpdateListener {
        void onUpdateDevice(Device device);
        void onUpdatePosition(Position position);
        void onUpdateEvent(Event event);
    }

    public synchronized void addListener(long userId, UpdateListener listener) {
        if (!listeners.containsKey(userId)) {
            listeners.put(userId, new HashSet<UpdateListener>());
        }
        listeners.get(userId).add(listener);
    }

    public synchronized void removeListener(long userId, UpdateListener listener) {
        if (!listeners.containsKey(userId)) {
            listeners.put(userId, new HashSet<UpdateListener>());
        }
        listeners.get(userId).remove(listener);
    }

}
