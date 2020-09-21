
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class MxtProtocol extends BaseProtocol {

    public MxtProtocol() {
        super("mxt");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                    pipeline.addLast("frameDecoder", new MxtFrameDecoder());
                    pipeline.addLast("objectDecoder", new MxtProtocolDecoder(MxtProtocol.this));
                }
        });
    }

}
