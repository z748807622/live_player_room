package com;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 单例 channel池
 */
public enum  ChannelHandlerPool {

    CHANNELGROUP;

    private ChannelGroup channelGroup = null;
    //public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ChannelHandlerPool(){
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public ChannelGroup getChannelGroup(){
        return channelGroup;
    }

}
