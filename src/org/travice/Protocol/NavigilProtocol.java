
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class NavigilProtocol extends BaseProtocol {

    public NavigilProtocol() {
        super("navigil");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new NavigilFrameDecoder());
                pipeline.addLast("objectDecoder", new NavigilProtocolDecoder(NavigilProtocol.this));
            }
        });
    }

}
