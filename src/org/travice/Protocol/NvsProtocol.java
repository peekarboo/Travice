
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class NvsProtocol extends BaseProtocol {

    public NvsProtocol() {
        super("nvs");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new NvsFrameDecoder());
                pipeline.addLast("objectDecoder", new NvsProtocolDecoder(NvsProtocol.this));
            }
        });
    }

}
