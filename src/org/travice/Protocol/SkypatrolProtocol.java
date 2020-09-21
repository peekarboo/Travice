
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class SkypatrolProtocol extends BaseProtocol {

    public SkypatrolProtocol() {
        super("skypatrol");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectDecoder", new SkypatrolProtocolDecoder(SkypatrolProtocol.this));
            }
        });
    }

}
