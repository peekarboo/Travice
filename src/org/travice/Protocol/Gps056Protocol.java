
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Gps056Protocol extends BaseProtocol {

    public Gps056Protocol() {
        super("gps056");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Gps056FrameDecoder());
                pipeline.addLast("objectDecoder", new Gps056ProtocolDecoder(Gps056Protocol.this));
            }
        });
    }

}
