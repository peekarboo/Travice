
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.nio.ByteOrder;
import java.util.List;

public class RitiProtocol extends BaseProtocol {

    public RitiProtocol() {
        super("riti");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 105, 2, 3, 0, true));
                pipeline.addLast("objectDecoder", new RitiProtocolDecoder(RitiProtocol.this));
            }
        });
    }

}
