
package org.travice.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class EsealProtocol extends BaseProtocol {

    public EsealProtocol() {
        super("eseal");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(1024));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new EsealProtocolEncoder());
                pipeline.addLast("objectDecoder", new EsealProtocolDecoder(EsealProtocol.this));
            }
        });
    }

}
