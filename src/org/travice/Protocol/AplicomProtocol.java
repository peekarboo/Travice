
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class AplicomProtocol extends BaseProtocol {

    public AplicomProtocol() {
        super("aplicom");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new AplicomFrameDecoder());
                pipeline.addLast("objectDecoder", new AplicomProtocolDecoder(AplicomProtocol.this));
            }
        });
    }

}
