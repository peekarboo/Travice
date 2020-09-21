
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class CalAmpProtocol extends BaseProtocol {

    public CalAmpProtocol() {
        super("calamp");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectDecoder", new CalAmpProtocolDecoder(CalAmpProtocol.this));
            }
        });
    }

}
