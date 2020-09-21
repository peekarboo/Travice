
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class Gt06Protocol extends BaseProtocol {

    public Gt06Protocol() {
        super("gt06");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_CUSTOM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Gt06FrameDecoder());
                pipeline.addLast("objectEncoder", new Gt06ProtocolEncoder());
                pipeline.addLast("objectDecoder", new Gt06ProtocolDecoder(Gt06Protocol.this));
            }
        });
    }

}
