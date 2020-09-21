
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class RoboTrackProtocol extends BaseProtocol {

    public RoboTrackProtocol() {
        super("robotrack");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new RoboTrackFrameDecoder());
                pipeline.addLast("objectDecoder", new RoboTrackProtocolDecoder(RoboTrackProtocol.this));
            }
        });
    }

}
