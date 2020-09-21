
package org.travice;

import io.netty.channel.ChannelHandler;

public interface PipelineBuilder {

    void addLast(String name, ChannelHandler handler);

}
