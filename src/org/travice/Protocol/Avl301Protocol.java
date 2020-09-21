
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Avl301Protocol extends BaseProtocol {

    public Avl301Protocol() {
        super("avl301");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(256, 2, 1, -3, 0));
                pipeline.addLast("objectDecoder", new Avl301ProtocolDecoder(Avl301Protocol.this));
            }
        });
    }

}
