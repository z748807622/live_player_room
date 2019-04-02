package com;

import com.zjy.IServer;
import com.zjy.handler.HandlerChain;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadVideoServer implements IServer {

    private static Logger log = LoggerFactory.getLogger(UploadVideoServer.class);

    private int port;

    public UploadVideoServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.SO_SNDBUF,1024*256)
                .option(ChannelOption.SO_RCVBUF,1024*256)
                .childHandler(new HandlerChain());

        try {
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("上传文件服务器关闭");
            defaultEventLoopGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }


}
