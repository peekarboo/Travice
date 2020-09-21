
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class Tk103Protocol extends BaseProtocol {

    public Tk103Protocol() {
        super("tk103");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_MODE_DEEP_SLEEP,
                Command.TYPE_MODE_POWER_SAVING,
                Command.TYPE_ALARM_SOS,
                Command.TYPE_SET_CONNECTION,
                Command.TYPE_SOS_NUMBER,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_GET_VERSION,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_SET_ODOMETER,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_OUTPUT_CONTROL);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Tk103FrameDecoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new Tk103ProtocolEncoder());
                pipeline.addLast("objectDecoder", new Tk103ProtocolDecoder(Tk103Protocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new Tk103ProtocolEncoder());
                pipeline.addLast("objectDecoder", new Tk103ProtocolDecoder(Tk103Protocol.this));
            }
        });
    }

}
