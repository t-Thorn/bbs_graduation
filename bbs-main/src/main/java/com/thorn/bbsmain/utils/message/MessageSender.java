package com.thorn.bbsmain.utils.message;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    @RabbitListener(queues = "unCheckedMsg")
    @RabbitHandler
    public void process(MessageObject msg) {
        MessageChannelInfo info = ChannelCache.get(msg.getUid());
        Channel msgChannel = info == null ? null : info.getChannel();
        if (msgChannel != null) {
            msgChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
        }
    }

    /**
     * 请求更新token后触发
     *
     * @param uid
     */
    @RabbitListener(queues = "newMsgAgain")
    @RabbitHandler
    public void sendNewMsgAgain(int uid) {
        MessageChannelInfo info = ChannelCache.get(uid);
        if (info == null) {
            return;
        }
        //验证channel是否可写
        Channel channel = info.getChannel();
        if (!channel.isWritable()) {
            return;
        }

        //验证token是否过期（不验证正确性），过期则返回请求，加入队列
        if (JWTUtil.isTokenExpired(info.getToken())) {
            return;
        }
        MessageObject msg = NewMessageCache.getAndReset(uid);
        if (msg == null) {
            return;
        }
        //根据消息类型处理消息
        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
    }

    /**
     * 有新消息产生是发送
     *
     * @param msg
     */
    @RabbitListener(queues = "newMsg")
    @RabbitHandler
    public void sendNewMsg(MessageObject msg) {
        //取出uid对应的channel和token
        MessageChannelInfo info = ChannelCache.get(msg.getUid());
        if (info == null) {
            return;
        }
        //验证channel是否可写
        Channel channel = info.getChannel();
        if (!channel.isWritable()) {
            return;
        }
        JSONObject jsonMsg = new JSONObject();

        //验证token是否过期（不验证正确性），过期则返回请求，加入队列
        if (JWTUtil.isTokenExpired(info.getToken())) {
            //过期
            jsonMsg.put("refresh", 1);
            if (msg.getType() == 1) {
                NewMessageCache.increase(msg.getUid());
            } else {
                NewMessageCache.updateAnnouncement(msg.getUid(), msg.getContent());
            }
            channel.writeAndFlush(new TextWebSocketFrame(jsonMsg.toJSONString()));
            return;
        }

        //根据消息类型处理消息
        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
    }

    /**
     * 有新消息产生是发送
     *
     * @param msg
     */
    @RabbitListener(queues = "broadcast")
    @RabbitHandler
    public void broadcast(String msg) {
        if (ChannelCache.getChannelGroup().size() == 0) {
            System.out.println("无通道");
            return;
        }

        ChannelCache.getChannelGroup().forEach(channel -> {
            //验证channel是否可写
            if (!channel.isWritable()) {
                System.out.println("通道不可写");
                return;
            }
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(
                    MessageObject.builder().uid(-1).content(msg).type(2).build())));
        });

    }
}
