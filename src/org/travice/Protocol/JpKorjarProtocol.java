
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class JpKorjarProtocol extends BaseProtocol {

    public JpKorjarProtocol() {
        super("jpkorjar");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, this.getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new JpKorjarFrameDecoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectDecoder", new JpKorjarProtocolDecoder(JpKorjarProtocol.this));
            }
        });
    }

}
