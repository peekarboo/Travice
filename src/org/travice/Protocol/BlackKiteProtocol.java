
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class BlackKiteProtocol extends BaseProtocol {

    public BlackKiteProtocol() {
        super("blackkite");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new GalileoFrameDecoder());
                pipeline.addLast("objectDecoder", new BlackKiteProtocolDecoder(BlackKiteProtocol.this));
            }
        });
    }

}
