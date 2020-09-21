
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.Context;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.List;

public class XexunProtocol extends BaseProtocol {

    public XexunProtocol() {
        super("xexun");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                boolean full = Context.getConfig().getBoolean(getName() + ".extended");
                if (full) {
                    pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(1024)); // tracker bug \n\r
                } else {
                    pipeline.addLast("frameDecoder", new XexunFrameDecoder());
                }
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new XexunProtocolEncoder());
                pipeline.addLast("objectDecoder", new XexunProtocolDecoder(XexunProtocol.this, full));
            }
        });
    }

}
