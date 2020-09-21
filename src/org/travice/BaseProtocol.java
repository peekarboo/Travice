
package org.travice;

import io.netty.buffer.Unpooled;
import org.travice.database.ActiveDevice;
import org.travice.helper.DataConverter;
import org.travice.model.Command;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseProtocol implements Protocol {

    private final String name;
    private final Set<String> supportedDataCommands = new HashSet<>();
    private final Set<String> supportedTextCommands = new HashSet<>();

    private StringProtocolEncoder textCommandEncoder = null;

    public BaseProtocol(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setSupportedDataCommands(String... commands) {
        supportedDataCommands.addAll(Arrays.asList(commands));
    }

    public void setSupportedTextCommands(String... commands) {
        supportedTextCommands.addAll(Arrays.asList(commands));
    }

    public void setSupportedCommands(String... commands) {
        supportedDataCommands.addAll(Arrays.asList(commands));
        supportedTextCommands.addAll(Arrays.asList(commands));
    }

    @Override
    public Collection<String> getSupportedDataCommands() {
        Set<String> commands = new HashSet<>(supportedDataCommands);
        commands.add(Command.TYPE_CUSTOM);
        return commands;
    }

    @Override
    public Collection<String> getSupportedTextCommands() {
        Set<String> commands = new HashSet<>(supportedTextCommands);
        commands.add(Command.TYPE_CUSTOM);
        return commands;
    }

    @Override
    public void sendDataCommand(ActiveDevice activeDevice, Command command) {
        if (supportedDataCommands.contains(command.getType())) {
            activeDevice.write(command);
        } else if (command.getType().equals(Command.TYPE_CUSTOM)) {
            String data = command.getString(Command.KEY_DATA);
            if (activeDevice.getChannel().pipeline().get("stringEncoder") != null) {
                activeDevice.write(data);
            } else {
                activeDevice.write(Unpooled.wrappedBuffer(DataConverter.parseHex(data)));
            }
        } else {
            throw new RuntimeException("Command " + command.getType() + " is not supported in protocol " + getName());
        }
    }

    public void setTextCommandEncoder(StringProtocolEncoder textCommandEncoder) {
        this.textCommandEncoder = textCommandEncoder;
    }

    @Override
    public void sendTextCommand(String destAddress, Command command) throws Exception {
        if (Context.getSmsManager() != null) {
            if (command.getType().equals(Command.TYPE_CUSTOM)) {
                Context.getSmsManager().sendMessageSync(destAddress, command.getString(Command.KEY_DATA), true);
            } else if (supportedTextCommands.contains(command.getType()) && textCommandEncoder != null) {
                Context.getSmsManager().sendMessageSync(destAddress,
                        (String) textCommandEncoder.encodeCommand(command), true);
            } else {
                throw new RuntimeException(
                        "Command " + command.getType() + " is not supported in protocol " + getName());
            }
        } else {
            throw new RuntimeException("SMS is not enabled");
        }
    }

}
