
package org.travice.database;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.travice.BaseProtocol;
import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Command;
import org.travice.model.Typed;
import org.travice.model.Position;

public class CommandsManager  extends ExtendedObjectManager<Command> {

    private final Map<Long, Queue<Command>> deviceQueues = new ConcurrentHashMap<>();

    private boolean queueing;

    public CommandsManager(DataManager dataManager, boolean queueing) {
        super(dataManager, Command.class);
        this.queueing = queueing;
    }

    public boolean checkDeviceCommand(long deviceId, long commandId) {
        return !getAllDeviceItems(deviceId).contains(commandId);
    }

    public boolean sendCommand(Command command) throws Exception {
        long deviceId = command.getDeviceId();
        if (command.getId() != 0) {
            command = getById(command.getId()).clone();
            command.setDeviceId(deviceId);
        }
        if (command.getTextChannel()) {
            Position lastPosition = Context.getIdentityManager().getLastPosition(deviceId);
            String phone = Context.getIdentityManager().getById(deviceId).getPhone();
            if (lastPosition != null) {
                BaseProtocol protocol = Context.getServerManager().getProtocol(lastPosition.getProtocol());
                protocol.sendTextCommand(phone, command);
            } else if (command.getType().equals(Command.TYPE_CUSTOM)) {
                if (Context.getSmsManager() != null) {
                    Context.getSmsManager().sendMessageSync(phone, command.getString(Command.KEY_DATA), true);
                } else {
                    throw new RuntimeException("SMS is not enabled");
                }
            } else {
                throw new RuntimeException("Command " + command.getType() + " is not supported");
            }
        } else {
            ActiveDevice activeDevice = Context.getConnectionManager().getActiveDevice(deviceId);
            if (activeDevice != null) {
                activeDevice.sendCommand(command);
            } else if (!queueing) {
                throw new RuntimeException("Device is not online");
            } else {
                getDeviceQueue(deviceId).add(command);
                return false;
            }
        }
        return true;
    }

    public Collection<Long> getSupportedCommands(long deviceId) {
        List<Long> result = new ArrayList<>();
        Position lastPosition = Context.getIdentityManager().getLastPosition(deviceId);
        for (long commandId : getAllDeviceItems(deviceId)) {
            Command command = getById(commandId);
            if (lastPosition != null) {
                BaseProtocol protocol = Context.getServerManager().getProtocol(lastPosition.getProtocol());
                if (command.getTextChannel() && protocol.getSupportedTextCommands().contains(command.getType())
                        || !command.getTextChannel()
                        && protocol.getSupportedDataCommands().contains(command.getType())) {
                    result.add(commandId);
                }
            } else if (command.getType().equals(Command.TYPE_CUSTOM)) {
                result.add(commandId);
            }
        }
        return result;
    }

    public Collection<Typed> getCommandTypes(long deviceId, boolean textChannel) {
        List<Typed> result = new ArrayList<>();
        Position lastPosition = Context.getIdentityManager().getLastPosition(deviceId);
        if (lastPosition != null) {
            BaseProtocol protocol = Context.getServerManager().getProtocol(lastPosition.getProtocol());
            Collection<String> commands;
            commands = textChannel ? protocol.getSupportedTextCommands() : protocol.getSupportedDataCommands();
            for (String commandKey : commands) {
                result.add(new Typed(commandKey));
            }
        } else {
            result.add(new Typed(Command.TYPE_CUSTOM));
        }
        return result;
    }

    public Collection<Typed> getAllCommandTypes() {
        List<Typed> result = new ArrayList<>();
        Field[] fields = Command.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("TYPE_")) {
                try {
                    result.add(new Typed(field.get(null).toString()));
                } catch (IllegalArgumentException | IllegalAccessException error) {
                    Log.warning(error);
                }
            }
        }
        return result;
    }

    private Queue<Command> getDeviceQueue(long deviceId) {
        if (!deviceQueues.containsKey(deviceId)) {
            deviceQueues.put(deviceId, new ConcurrentLinkedQueue<Command>());
        }
        return deviceQueues.get(deviceId);
    }

    public void sendQueuedCommands(ActiveDevice activeDevice) {
        Queue<Command> deviceQueue = deviceQueues.get(activeDevice.getDeviceId());
        if (deviceQueue != null) {
            Command command = deviceQueue.poll();
            while (command != null) {
                activeDevice.sendCommand(command);
                command = deviceQueue.poll();
            }
        }
    }

}
