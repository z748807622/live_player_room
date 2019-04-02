package com.zjy.handler;

import com.zjy.UDPStreamServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger log = LoggerFactory.getLogger(UDPServerHandler.class);
    private static ChannelHandlerContext CTX;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("UDP转流服务器通道已连接");
        this.CTX = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            DatagramPacket datagramPacket = (DatagramPacket) msg;
            ByteBuf bb = Unpooled.copiedBuffer(datagramPacket.content());
            //TODO 向websocket转发bb
            System.out.println("----");
            UDPStreamServer.getTransponStreamServerChannelGroup().writeAndFlush(new BinaryWebSocketFrame(bb));
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("udp转发服务器出错：" + cause.getMessage());
    }
}
