
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.nio.ByteOrder;
import java.util.List;

public class NavisProtocol extends BaseProtocol {

    public NavisProtocol() {
        super("navis");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4 * 1024, 12, 2, 2, 0, true));
                pipeline.addLast("objectDecoder", new NavisProtocolDecoder(NavisProtocol.this));
            }
        });
    }

}
