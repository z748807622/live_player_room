package com.zjy.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.internal.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zjy.config.*;

public class UploadVideoHandler extends SimpleChannelInboundHandler<Object> {


    private static Logger logger = LoggerFactory.getLogger(UploadVideoHandler.class);

    private boolean first = true;
    private FileOutputStream fileOutputStream;
    private String fileName = null;
    private ByteChannel byteChannel;


    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        first = true;
        logger.info("{} channelActicve ",ctx.channel().localAddress().toString());
    }

    /*
     * channelInactive
     *
     * channel 	通道
     * Inactive 不活跃的
     *
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     *
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString() + " channelInactive");
        // 关闭流
        loginOut();
        first = true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocket(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 握手连接
     * @param ctx
     * @param request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            logger.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
                Config.UPLOAD_VIDEO_SERVER_WEBSOCKET_URL, null, true);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 动态加入websocket的编解码处理
            handshaker.handshake(ctx.channel(), request);
            /*UserInfo userInfo = new UserInfo();
            userInfo.setAddr(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
            // 存储已经连接的Channel
            UserInfoManager.addChannel(ctx.channel());*/
            logger.info("上传文件 websocket 连接成功");
            //ctx.pipeline().writeAndFlush(Unpooled.copiedBuffer("连接成功!".getBytes()));
            ctx.writeAndFlush(new TextWebSocketFrame("{'code':111,'msg':'连接成功'}"));
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            logger.info("上传-{}-  退出", ctx.channel().localAddress().toString());
            return;
        }
        // 判断是否Ping消息
        if (frame instanceof PingWebSocketFrame) {
            logger.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 支持文本消息
        if (frame instanceof TextWebSocketFrame) {
            //throw new UnsupportedOperationException(frame.getClass().getName() + " frame type not supported");
            String message = ((TextWebSocketFrame) frame).text();
            Channel channel = ctx.channel();
            logger.info("上传服务器收到消息：{}", message);
            try {
                JSONObject json = JSON.parseObject(message);
                if (json.containsKey("fileName")) {
                    fileName = json.getString("fileName");
                    File file = new File(Config.UPLOAD_VIDEO_FILE_DIR + fileName);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                            ctx.writeAndFlush(new TextWebSocketFrame("{'code':200,'msg':'创建文件:"+fileName+"'}"));
                            fileOutputStream = new FileOutputStream(file);
                            byteChannel = fileOutputStream.getChannel();//用nio将数据写入文件
                        } catch (IOException e) {
                            e.printStackTrace();
                            ctx.writeAndFlush(new TextWebSocketFrame("{'code':1,'msg':'创建文件失败'}"));
                            loginOut();
                        }
                    } else {
                        ctx.writeAndFlush(new TextWebSocketFrame("{'code':1,'msg':'文件名已存在'}"));
                        loginOut();
                        return;
                    }
                }else if(json.containsKey("finish")){
                    ctx.writeAndFlush(new TextWebSocketFrame("{'code':777,'msg':'文件接收成功'}"));
                    logger.info("{'code':777,'msg':'文件接收成功'}");
                    loginOut();
                }
                else {
                    ctx.writeAndFlush(new TextWebSocketFrame("{'code':1,'msg':'参数错误'}"));
                    loginOut();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.writeAndFlush(new TextWebSocketFrame("{'code':1,'msg':'参数错误'}"));
                loginOut();
            }
            return;
        }

        if (frame instanceof BinaryWebSocketFrame) {
            if (StringUtil.isNullOrEmpty(fileName)) {
                ctx.writeAndFlush(new TextWebSocketFrame("{'code':1,'msg':'未选择文件名'}"));
                logger.info("{'code':1,'msg':'未选择文件名'}");
                loginOut();
                return;
            }
            //logger.info("接收到上传文件数据");
            ByteBuf buf = frame.content();
            // 开始处理文件信息
            ByteBuffer buffer = ByteBuffer.allocate(buf.readableBytes());
            buf.readBytes(buffer);
            //System.out.println("本次接收内容长度：" + frame.toString().length());
            try {
                buffer.flip();//转为读状态
                byteChannel.write(buffer);
                ctx.writeAndFlush(new TextWebSocketFrame("0"));//代表接收成功，可以发送下一段了
            } catch (IOException e) {
                e.printStackTrace();
                loginOut();
            }
        }
    }

    /*
     * channelReadComplete
     *
     * channel  通道
     * Read     读取
     * Complete 完成
     *
     * 在通道读取完成后会在这个方法里通知，对应可以做刷新操作
     * ctx.flush()
     *
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /*
     * exceptionCaught
     *
     * exception	异常
     * Caught		抓住
     *
     * 抓住异常，当发生异常的时候，可以做一些相应的处理，比如打印日志、关闭链接
     *
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.error("异常信息： {}",cause.getMessage());
    }

    /**
     * 关闭通道和文件
     */
    private void loginOut(){
        if(fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(byteChannel != null){
            try {
                byteChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.warn("文件通道关闭");
    }

}
