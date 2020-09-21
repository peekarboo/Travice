
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class UlbotechProtocol extends BaseProtocol {

    public UlbotechProtocol() {
        super("ulbotech");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new UlbotechFrameDecoder());
                pipeline.addLast("objectDecoder", new UlbotechProtocolDecoder(UlbotechProtocol.this));
            }
        });
    }

}
