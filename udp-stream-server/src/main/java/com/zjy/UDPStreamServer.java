package com.zjy;

import com.zjy.handler.UDPServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPStreamServer implements IServer {

    /**
     * websocket转发视频 channel组
     */
    private static ChannelGroup transponStreamServerChannelGroup;
    private int port;

    public UDPStreamServer(ChannelGroup transponStreamServerChannelGroup,int port){
        this.transponStreamServerChannelGroup = transponStreamServerChannelGroup;
        this.port = port;
    }

    //private static Logger log = Logger.getLogger(UDPStreamServer.class);

    public void run() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new UDPServerHandler());
        try {
            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            //log.error(e.getMessage());
        }finally {
            eventLoopGroup.shutdownGracefully();
            //log.warn("udp转发服务器关闭");
        }
    }

    public static ChannelGroup getTransponStreamServerChannelGroup(){
        return transponStreamServerChannelGroup;
    }

}
