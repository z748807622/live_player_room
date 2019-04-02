package com;

import com.handler.HandlerChain;
import com.zjy.IServer;
import com.zjy.UDPStreamServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketVideoServer implements IServer {

    private static Logger log = LoggerFactory.getLogger(WebsocketVideoServer.class);

    public int port;
    public int udpPort;

    public WebsocketVideoServer(int port,int udpPort){
        this.port = port;
        this.udpPort = udpPort;
    }

    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.SO_SNDBUF,1024*256)
                .option(ChannelOption.SO_RCVBUF,1024*256)
                .childHandler(new HandlerChain(defaultEventLoop));

        try {
            //bootstrap.bind(port).sync().channel().closeFuture().sync();

            /**
             * websocket服务器
             */
            Thread websocketServer = new Thread(()->{
                try {
                    bootstrap.bind(port).sync().channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },"websocket服务器");

            /**
             * udp服务器
             */
            Thread udpServer = new Thread(()->{
                new UDPStreamServer(ChannelHandlerPool.CHANNELGROUP.getChannelGroup(),udpPort).run();
            },"udp服务器");
            websocketServer.start();
            udpServer.start();
            log.info("websocket服务器启动");
            log.info("udp服务器启动成功");
            websocketServer.join();
            udpServer.join();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            defaultEventLoop.shutdownGracefully();
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}
