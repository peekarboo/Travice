
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class OrionProtocol extends BaseProtocol {

    public OrionProtocol() {
        super("orion");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new OrionFrameDecoder());
                pipeline.addLast("objectDecoder", new OrionProtocolDecoder(OrionProtocol.this));
            }
        });
    }

}
