
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class TmgProtocol extends BaseProtocol {

    public TmgProtocol() {
        super("tmg");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new TmgFrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectDecoder", new TmgProtocolDecoder(TmgProtocol.this));
            }
        });
    }

}
