
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Vt200Protocol extends BaseProtocol {

    public Vt200Protocol() {
        super("vt200");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Vt200FrameDecoder());
                pipeline.addLast("objectDecoder", new Vt200ProtocolDecoder(Vt200Protocol.this));
            }
        });
    }

}
