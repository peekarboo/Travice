
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class SabertekProtocol extends BaseProtocol {

    public SabertekProtocol() {
        super("sabertek");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new SabertekFrameDecoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectDecoder", new SabertekProtocolDecoder(SabertekProtocol.this));
            }
        });
    }

}
