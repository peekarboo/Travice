
package org.travice.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class SiwiProtocol extends BaseProtocol {

    public SiwiProtocol() {
        super("siwi");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(1024));
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectDecoder", new SiwiProtocolDecoder(SiwiProtocol.this));
            }
        });
    }

}
