
package org.travice;

import org.travice.database.ActiveDevice;
import org.travice.model.Command;

import java.util.Collection;
import java.util.List;

public interface Protocol {

    String getName();

    Collection<String> getSupportedDataCommands();

    void sendDataCommand(ActiveDevice activeDevice, Command command);

    void initTrackerServers(List<TrackerServer> serverList);

    Collection<String> getSupportedTextCommands();

    void sendTextCommand(String destAddress, Command command) throws Exception;

}
