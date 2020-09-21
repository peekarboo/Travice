
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class BceProtocol extends BaseProtocol {

    public BceProtocol() {
        super("bce");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new BceFrameDecoder());
                pipeline.addLast("objectDecoder", new BceProtocolDecoder(BceProtocol.this));
            }
        });
    }

}
