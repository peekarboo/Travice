
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class TlvProtocol extends BaseProtocol {

    public TlvProtocol() {
        super("tlv");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, '\0'));
                pipeline.addLast("objectDecoder", new TlvProtocolDecoder(TlvProtocol.this));
            }
        });
    }

}
