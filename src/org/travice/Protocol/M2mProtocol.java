
package org.travice.protocol;

import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class M2mProtocol extends BaseProtocol {

    public M2mProtocol() {
        super("m2m");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new FixedLengthFrameDecoder(23));
                pipeline.addLast("objectDecoder", new M2mProtocolDecoder(M2mProtocol.this));
            }
        });
    }

}
