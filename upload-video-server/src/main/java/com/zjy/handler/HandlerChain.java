package com.zjy.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HandlerChain extends ChannelInitializer<SocketChannel> {

    private DefaultEventLoopGroup defaultEventLoopGroup;

    public HandlerChain(){
        super();
    }

    public HandlerChain(DefaultEventLoopGroup defaultEventLoopGroup){
        this.defaultEventLoopGroup = defaultEventLoopGroup;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(defaultEventLoopGroup,
                    //请求解码器
                    new HttpServerCodec(),
                    //将多个消息转换成单一的消息对象
                    new HttpObjectAggregator(Integer.MAX_VALUE),
                    //支持异步发送大的码流，一般用于发送文件流
                    //new StringDecoder(),
                    //new StringEncoder(),
                    new ChunkedWriteHandler(),
                    //检测链路是否读空闲
                    //new IdleStateHandler(60, 0, 0),
                    //处理握手和认证
                    //new UserAuthHandler(),
                    //处理消息的发送
                    new UploadVideoHandler()

                );
    }
}
