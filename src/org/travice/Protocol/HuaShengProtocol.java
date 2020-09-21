
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class HuaShengProtocol extends BaseProtocol {

    public HuaShengProtocol() {
        super("huasheng");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new HuaShengFrameDecoder());
                pipeline.addLast("objectDecoder", new HuaShengProtocolDecoder(HuaShengProtocol.this));
            }
        });
    }

}
