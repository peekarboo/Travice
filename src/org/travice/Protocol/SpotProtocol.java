
package org.travice.protocol;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;

public class SpotProtocol extends BaseProtocol {

    public SpotProtocol() {
        super("spot");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("httpEncoder", new HttpResponseEncoder());
                pipeline.addLast("httpDecoder", new HttpRequestDecoder());
                pipeline.addLast("httpAggregator", new HttpObjectAggregator(65535));
                pipeline.addLast("objectDecoder", new SpotProtocolDecoder(SpotProtocol.this));
            }
        });
    }

}
