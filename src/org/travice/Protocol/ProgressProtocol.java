
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.nio.ByteOrder;
import java.util.List;

public class ProgressProtocol extends BaseProtocol {

    public ProgressProtocol() {
        super("progress");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 2, 4, 0, true));
                pipeline.addLast("objectDecoder", new ProgressProtocolDecoder(ProgressProtocol.this));
            }
        });
    }

}
