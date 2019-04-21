package com.zjy;

import com.zjy.handler.HandlerChain;
import com.zjy.handler.UserInfoManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebsocketChatServer implements IServer {

    private static Logger log = LoggerFactory.getLogger(WebsocketChatServer.class);

    private int port;

    public WebsocketChatServer(int port){
        this.port = port;
    }

    @Override
    public void run() {

        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup(8, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "DEFAULTEVENTLOOPGROUP_" + index.incrementAndGet());
            }
        });

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "BOSS_" + index.incrementAndGet());
            }
        });

        NioEventLoopGroup workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WORK_" + index.incrementAndGet());
            }
        });

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new HandlerChain());

        log.info("聊天服务器准备启动");
        ScheduledExecutorService executorService = Executors
                .newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(()->{
            UserInfoManager.scanNotActiveChannel();
        },3,60, TimeUnit.SECONDS);

        executorService.scheduleAtFixedRate(()->{
            UserInfoManager.broadCastPing();
        },3,40,TimeUnit.SECONDS);

        /*executorService.scheduleAtFixedRate(()->{
            UserInfoManager.sendLiveCmdInfo("lalalallala");
        },3,3,TimeUnit.SECONDS);*/

        try {
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("websocket聊天室关闭");
            defaultEventLoopGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

}
