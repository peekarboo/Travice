
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Xt2400Protocol extends BaseProtocol {

    public Xt2400Protocol() {
        super("xt2400");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectDecoder", new Xt2400ProtocolDecoder(Xt2400Protocol.this));
            }
        });
    }

}
