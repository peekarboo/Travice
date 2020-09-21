
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class T800xProtocol extends BaseProtocol {

    public T800xProtocol() {
        super("t800x");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 3, 2, -5, 0));
                pipeline.addLast("objectEncoder", new T800xProtocolEncoder());
                pipeline.addLast("objectDecoder", new T800xProtocolDecoder(T800xProtocol.this));
            }
        });
    }

}
