package com.thorn.bbsmain.utils.message;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Configuration
@Component
@Slf4j
public class MessageHandlerBuilder {

    @Autowired
    ChannelHandler handler;
    private Channel channel;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public ChannelFuture buildMessageHandler() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(initializer());
        ChannelFuture future = bootstrap.bind(8899).syncUninterruptibly();
        log.info("消息服务器初始化成功");
        channel = future.channel();
        return future;
    }

    private ChannelInitializer<SocketChannel> initializer() {

        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //http编码器
                pipeline.addLast(new HttpServerCodec());
                //以块的形式写
                pipeline.addLast(new ChunkedWriteHandler());
                //将块组装成对象（比如该处用到的文本对象）
                pipeline.addLast(new HttpObjectAggregator(1024));
                //添加对于websocket的支持,参数配置的是访问的路径
                pipeline.addLast(new WebSocketServerProtocolHandler("/msg"));
                //粘包处理，采用换行符标识
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                //业务处理逻辑
                pipeline.addLast(handler);
                //心跳检测
                pipeline.addLast("ping", new IdleStateHandler(25, 15, 10, TimeUnit.SECONDS));
            }
        };

    }

    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
