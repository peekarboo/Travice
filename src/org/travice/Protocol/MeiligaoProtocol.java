
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class MeiligaoProtocol extends BaseProtocol {

    public MeiligaoProtocol() {
        super("meiligao");
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_GEOFENCE,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_REBOOT_DEVICE);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new MeiligaoFrameDecoder());
                pipeline.addLast("objectEncoder", new MeiligaoProtocolEncoder());
                pipeline.addLast("objectDecoder", new MeiligaoProtocolDecoder(MeiligaoProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new MeiligaoProtocolEncoder());
                pipeline.addLast("objectDecoder", new MeiligaoProtocolDecoder(MeiligaoProtocol.this));
            }
        });
    }

}
