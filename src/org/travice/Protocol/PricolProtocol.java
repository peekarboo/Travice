
package org.travice.protocol;

import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class PricolProtocol extends BaseProtocol {

    public PricolProtocol() {
        super("pricol");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new FixedLengthFrameDecoder(64));
                pipeline.addLast("objectDecoder", new PricolProtocolDecoder(PricolProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectDecoder", new PricolProtocolDecoder(PricolProtocol.this));
            }
        });
    }

}
