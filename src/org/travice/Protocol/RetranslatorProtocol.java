
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class RetranslatorProtocol extends BaseProtocol {

    public RetranslatorProtocol() {
        super("retranslator");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new RetranslatorFrameDecoder());
                pipeline.addLast("objectDecoder", new RetranslatorProtocolDecoder(RetranslatorProtocol.this));
            }
        });
    }

}
