
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class CellocatorProtocol extends BaseProtocol {

    public CellocatorProtocol() {
        super("cellocator");
        setSupportedDataCommands(
                Command.TYPE_OUTPUT_CONTROL);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CellocatorFrameDecoder());
                pipeline.addLast("objectEncoder", new CellocatorProtocolEncoder());
                pipeline.addLast("objectDecoder", new CellocatorProtocolDecoder(CellocatorProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new CellocatorProtocolEncoder());
                pipeline.addLast("objectDecoder", new CellocatorProtocolDecoder(CellocatorProtocol.this));
            }
        });
    }

}
