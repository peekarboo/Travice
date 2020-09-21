
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class ThinkRaceProtocol extends BaseProtocol {

    public ThinkRaceProtocol() {
        super("thinkrace");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 2 + 12 + 1 + 1, 2, 2, 0));
                pipeline.addLast("objectDecoder", new ThinkRaceProtocolDecoder(ThinkRaceProtocol.this));
            }
        });
    }

}
