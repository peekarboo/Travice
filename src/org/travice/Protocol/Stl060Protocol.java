
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Stl060Protocol extends BaseProtocol {

    public Stl060Protocol() {
        super("stl060");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Stl060FrameDecoder(1024));
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectDecoder", new Stl060ProtocolDecoder(Stl060Protocol.this));
            }
        });
    }

}
