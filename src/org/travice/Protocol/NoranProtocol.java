
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class NoranProtocol extends BaseProtocol {

    public NoranProtocol() {
        super("noran");
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new NoranProtocolEncoder());
                pipeline.addLast("objectDecoder", new NoranProtocolDecoder(NoranProtocol.this));
            }
        });
    }

}
