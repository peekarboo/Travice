
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class Tt8850Protocol extends BaseProtocol {

    public Tt8850Protocol() {
        super("tt8850");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, "$"));
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectDecoder", new Tt8850ProtocolDecoder(Tt8850Protocol.this));
            }
        });
    }

}
