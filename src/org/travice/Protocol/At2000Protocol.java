
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class At2000Protocol extends BaseProtocol {

    public At2000Protocol() {
        super("at2000");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new At2000FrameDecoder());
                pipeline.addLast("objectDecoder", new At2000ProtocolDecoder(At2000Protocol.this));
            }
        });
    }

}
