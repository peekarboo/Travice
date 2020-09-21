
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class TramigoProtocol extends BaseProtocol {

    public TramigoProtocol() {
        super("tramigo");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new TramigoFrameDecoder());
                pipeline.addLast("objectDecoder", new TramigoProtocolDecoder(TramigoProtocol.this));
            }
        });
    }

}
