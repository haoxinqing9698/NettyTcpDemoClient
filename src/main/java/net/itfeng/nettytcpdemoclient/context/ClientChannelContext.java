package net.itfeng.nettytcpdemoclient.context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

/**
 * 用于保存客户端与服务端连接的channel
 *
 * @author itfeng
 * @since 2024/2/25 09:43
 */
public class ClientChannelContext {

    private static Channel channel=null;

    private static long lastInboundTime = System.currentTimeMillis();

    public static void put(Channel channel){
        ClientChannelContext.channel = channel;
    }

    public static Channel getChannel(){
        return ClientChannelContext.channel;
    }

    public static void updateLastInboundTime(){
        ClientChannelContext.lastInboundTime = System.currentTimeMillis();
    }

    public static long getLastInboundTime(){
        return ClientChannelContext.lastInboundTime;
    }
}
