package com.thorn.bbsmain;

import com.thorn.bbsmain.utils.message.MessageHandlerBuilder;
import io.netty.channel.ChannelFuture;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@EnableAsync
@EnableTransactionManagement
//@EnableRetry
@MapperScan("com.thorn.bbsmain.mapper")
@EnableCaching
@SpringBootApplication
public class BbsMainApplication implements CommandLineRunner {

    @Autowired
    private MessageHandlerBuilder ws;

    public static void main(String[] args) {
        SpringApplication.run(BbsMainApplication.class, args);

    }

    // 注意这里的 run 方法是重载自 CommandLineRunner
    @Override
    public void run(String... args) throws Exception {
        ChannelFuture future = ws.buildMessageHandler();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ws.destroy();
            }
        });

        future.channel().closeFuture().syncUninterruptibly();
    }

}
