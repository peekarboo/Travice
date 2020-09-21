
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.List;

public class TzoneProtocol extends BaseProtocol {

    public TzoneProtocol() {
        super("tzone");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(256, 2, 2, 2, 0));
                pipeline.addLast("objectDecoder", new TzoneProtocolDecoder(TzoneProtocol.this));
            }
        });
    }

}
