
package org.travice;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public abstract class TrackerServer {

    private final boolean datagram;
    private final AbstractBootstrap bootstrap;

    public boolean isDatagram() {
        return datagram;
    }

    public TrackerServer(boolean datagram, String protocol) {
        this.datagram = datagram;

        address = Context.getConfig().getString(protocol + ".address");
        port = Context.getConfig().getInteger(protocol + ".port");

        BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, protocol) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                TrackerServer.this.addProtocolHandlers(pipeline);
            }
        };

        if (datagram) {

            this.bootstrap = new Bootstrap()
                    .group(EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioDatagramChannel.class)
                    .handler(pipelineFactory);

        } else {

            this.bootstrap = new ServerBootstrap()
                    .group(EventLoopGroupFactory.getBossGroup(), EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(pipelineFactory);

        }
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public void start() throws Exception {
        InetSocketAddress endpoint;
        if (address == null) {
            endpoint = new InetSocketAddress(port);
        } else {
            endpoint = new InetSocketAddress(address, port);
        }

        Channel channel = bootstrap.bind(endpoint).sync().channel();
        if (channel != null) {
            getChannelGroup().add(channel);
        }
    }

    public void stop() {
        channelGroup.close().awaitUninterruptibly();
    }

}
