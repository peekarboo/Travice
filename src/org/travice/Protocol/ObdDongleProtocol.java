
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class ObdDongleProtocol extends BaseProtocol {

    public ObdDongleProtocol() {
        super("obddongle");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1099, 20, 2, 3, 0));
                pipeline.addLast("objectDecoder", new ObdDongleProtocolDecoder(ObdDongleProtocol.this));
            }
        });
    }

}
