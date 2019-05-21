package com.thorn.bbsmain.utils.message;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.utils.MysqlJdbc;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
//用于管道创建，可重复删除和创建，
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //检验token合法性，并将channel保存到cache中
        JSONObject json = (JSONObject) JSONObject.parse(msg.text());
        int uid = validateAndSaveToCache(ctx, json);
        //返回的时候也必须要是TextWebSocketFrame对象而不能是普通的字符串
        //若是refresh存在说明只是刷新token,此handler不做不做其他处理
        if (json.getString("refresh") == null) {
            //System.out.println("获取未读消息");
            rabbitTemplate.convertAndSend("unCheckedMsg",
                    MessageObject.builder().uid(uid).num(MysqlJdbc.getUncheckedMessage(uid)).build());
/*            rabbitTemplate.convertAndSend("newMsg",
                    MessageObject.builder().uid(uid).content("<p>测试系统消息</p>").build());*/
        } else {
            // System.out.println("再次请求发送消息");
            //触发再次发送新消息事件
            rabbitTemplate.convertAndSend("newMsgAgain", uid);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //断开连接
        ChannelCache.remove(ctx.channel().id().asShortText());
        ChannelCache.getChannelGroup().remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    private int validateAndSaveToCache(ChannelHandlerContext ctx, JSONObject json) throws IllegalRequestException {
        String token = json.getString("token");
        if (token == null) {
            log.warn("请求token为空，ip：{}", ctx.channel().remoteAddress());
            throw new IllegalRequestException("请求token为空");
        }
        String email = JWTUtil.getEmail(token);
        String password =
                MysqlJdbc.getPassword(email);
        if (!JWTUtil.verify(token, email, password)) {
            //log.warn("token验证错误，ip：{}", ctx.channel().remoteAddress());
            throw new IllegalRequestException("token验证错误");
        }
        Integer uid = MysqlJdbc.getUID(email);
        if (uid == null) {
            log.error("token中的email：{}无法找到uid", email);
            throw new IllegalRequestException("非法token");
        }
        ChannelCache.put(uid, ctx.channel(), token);
        ChannelCache.getChannelGroup().add(ctx.channel());
        return uid;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("获取消息异常：" + cause.getMessage());
        ctx.close();
    }
}
