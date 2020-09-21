
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class V680Protocol extends BaseProtocol {

    public V680Protocol() {
        super("v680");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, "##"));
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectDecoder", new V680ProtocolDecoder(V680Protocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectDecoder", new V680ProtocolDecoder(V680Protocol.this));
            }
        });
    }

}
