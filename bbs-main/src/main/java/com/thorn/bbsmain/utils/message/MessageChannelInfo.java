package com.thorn.bbsmain.utils.message;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 消息缓存的实体类
 */
@Data
@AllArgsConstructor
public class MessageChannelInfo {
    private Channel channel;

    private String token;
}
