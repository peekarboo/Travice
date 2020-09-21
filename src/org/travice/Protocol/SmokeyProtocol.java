
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class SmokeyProtocol extends BaseProtocol {

    public SmokeyProtocol() {
        super("smokey");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectDecoder", new SmokeyProtocolDecoder(SmokeyProtocol.this));
            }
        });
    }

}
