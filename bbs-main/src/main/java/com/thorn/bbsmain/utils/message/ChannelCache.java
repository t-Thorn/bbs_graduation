package com.thorn.bbsmain.utils.message;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelCache {
    //保存用户管道，uid：channel
    private static ConcurrentHashMap<Integer, MessageChannelInfo> channels = new ConcurrentHashMap<>();
    /**
     * 用来remove
     */
    private static ConcurrentHashMap<String, Integer> channelIndex = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void put(int uid, Channel channel, String token) {
        channels.put(uid, new MessageChannelInfo(channel, token));
        channelIndex.put(channel.id().asShortText(), uid);
    }

    public static MessageChannelInfo get(int uid) {
        if (channels == null) {
            return null;
        }
        return channels.get(uid);
    }

    public static void remove(String channelID) {
        if (channelIndex.containsKey(channelID)) {
            channels.remove(channelIndex.get(channelID));
            channelIndex.remove(channelID);
        }
    }
}
