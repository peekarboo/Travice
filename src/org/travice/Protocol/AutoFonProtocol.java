
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class AutoFonProtocol extends BaseProtocol {

    public AutoFonProtocol() {
        super("autofon");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new AutoFonFrameDecoder());
                pipeline.addLast("objectDecoder", new AutoFonProtocolDecoder(AutoFonProtocol.this));
            }
        });
    }

}
