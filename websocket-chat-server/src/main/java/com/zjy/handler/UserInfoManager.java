package com.zjy.handler;

import com.alibaba.fastjson.JSONObject;
import com.zjy.entity.UserInfo;
import com.zjy.proto.*;
import com.zjy.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoManager.class);

    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private static ConcurrentMap<Channel, UserInfo> userInfos = new ConcurrentHashMap<>();
    private static AtomicInteger userCount = new AtomicInteger(0);
    private static Channel adminChannel = null;
    private final static Object adminChannelLock = new Object();

    public static void addChannel(Channel channel) {
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()) {
            logger.error("channel is not active, address: {}", remoteAddr);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setAddr(remoteAddr);
        userInfo.setChannel(channel);
        userInfo.setTime(System.currentTimeMillis());
        userInfos.put(channel, userInfo);
    }

    public static boolean saveUser(Channel channel, String nick) {
        UserInfo userInfo = userInfos.get(channel);
        if (userInfo == null) {
            return false;
        }
        if (!channel.isActive()) {
            logger.error("channel is not active, address: {}, nick: {}", userInfo.getAddr(), nick);
            return false;
        }
        // 增加一个认证用户
        userCount.incrementAndGet();
        userInfo.setNick(nick);
        userInfo.setAuth(true);
        userInfo.setUserId();
        userInfo.setAdmin(StringUtils.equals("管理员",nick));//判断是否为管理员
        if (StringUtils.equals("管理员",nick)){
            synchronized (adminChannelLock){
                adminChannel = channel;
            }
        }
        userInfo.setTime(System.currentTimeMillis());
        return true;
    }

    /**
     * 从缓存中移除Channel，并且关闭Channel
     *
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        try {
            logger.warn("channel will be remove, address is :{}", NettyUtil.parseChannelRemoteAddr(channel));
            rwLock.writeLock().lock();
            channel.close();
            UserInfo userInfo = userInfos.get(channel);
            if (userInfo != null) {
                UserInfo tmp = userInfos.remove(channel);
                if (tmp != null && tmp.isAuth()) {
                    // 减去一个认证用户
                    userCount.decrementAndGet();
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    /**
     * 广播普通消息
     *
     * @param message
     */
    public static void broadcastMess(int uid, String nick, String message) {
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                rwLock.readLock().lock();
                Set<Channel> keySet = userInfos.keySet();
                for (Channel ch : keySet) {
                    UserInfo userInfo = userInfos.get(ch);
                    if (userInfo == null || !userInfo.isAuth()) continue;
                    ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(uid, nick, message)));
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * 广播系统消息
     */
    public static void broadCastInfo(int code, Object mess) {
        try {
            rwLock.readLock().lock();
            Set<Channel> keySet = userInfos.keySet();
            for (Channel ch : keySet) {
                UserInfo userInfo = userInfos.get(ch);
                if (userInfo == null || !userInfo.isAuth()) continue;
                ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 发送直播cmd信息
     * @param mes
     */
    public static void sendLiveCmdInfo(String mes){
        if (adminChannel != null && adminChannel.isActive()){
            Map<String,String> res = new HashMap<>();
            res.put("code","9999");
            res.put("mess",mes);
            adminChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(res)));
        }
    }

    public static void broadCastPing() {
        try {
            rwLock.readLock().lock();
            logger.info("broadCastPing userCount: {}", userCount.intValue());
            Set<Channel> keySet = userInfos.keySet();
            for (Channel ch : keySet) {
                UserInfo userInfo = userInfos.get(ch);
                if (userInfo == null || !userInfo.isAuth()) continue;
                ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPingProto()));
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     *
     * @param nickName nickName
     * @param isFlag false禁言  true解禁
     */
    public static void bannedByNickName(String nickName, boolean isFlag){
        try{
         rwLock.readLock().lock();
         Set<Channel> keSet = userInfos.keySet();
         for (Channel ch : keSet){
             UserInfo userInfo = userInfos.get(ch);
             if (userInfo != null && userInfo.getNick() != null && userInfo.getNick().equals(nickName)){
                 if (isFlag){
                     userInfo.setAllow(false);
                 }else {
                     userInfo.setAllow(true);
                 }
             }
         }
        }finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 发送系统消息
     *
     * @param code
     * @param mess
     */
    public static void sendInfo(Channel channel, int code, Object mess) {
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
    }

    public static void sendPong(Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPongProto()));
    }

    /**
     * 扫描并关闭失效的Channel
     */
    public static void scanNotActiveChannel() {
        Set<Channel> keySet = userInfos.keySet();
        for (Channel ch : keySet) {
            UserInfo userInfo = userInfos.get(ch);
            if (userInfo == null) continue;
            if (!ch.isOpen() || !ch.isActive() || (!userInfo.isAuth() &&
                    (System.currentTimeMillis() - userInfo.getTime()) > 10000)) {
                removeChannel(ch);
            }
        }
    }


    public static UserInfo getUserInfo(Channel channel) {
        return userInfos.get(channel);
    }

    public static ConcurrentMap<Channel, UserInfo> getUserInfos() {
        return userInfos;
    }

    public static int getAuthUserCount() {
        return userCount.get();
    }

    public static void updateUserTime(Channel channel) {
        UserInfo userInfo = getUserInfo(channel);
        if (userInfo != null) {
            userInfo.setTime(System.currentTimeMillis());
        }
    }

}
