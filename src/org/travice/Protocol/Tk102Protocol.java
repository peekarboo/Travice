
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Tk102Protocol extends BaseProtocol {

    public Tk102Protocol() {
        super("tk102");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 1 + 1 + 10, 1, 1, 0));
                pipeline.addLast("objectDecoder", new Tk102ProtocolDecoder(Tk102Protocol.this));
            }
        });
    }

}
