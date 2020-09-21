
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class EgtsProtocol extends BaseProtocol {

    public EgtsProtocol() {
        super("egts");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new EgtsFrameDecoder());
                pipeline.addLast("objectDecoder", new EgtsProtocolDecoder(EgtsProtocol.this));
            }
        });
    }

}
