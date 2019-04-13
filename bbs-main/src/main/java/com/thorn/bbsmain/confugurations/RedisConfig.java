package com.thorn.bbsmain.confugurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@Slf4j
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    /**
     * redis端口号
     */

    @Value("${spring.redis.port}")
    private Integer port;

    /**
     * redis密码
     */

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    /**
     * JedisPoolConfig 连接池
     *
     * @return
     */

    @Bean
    public JedisPool redisPoolFactory() {
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            // JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, null);
            log.info("初始化Redis连接池JedisPool成功!" + " Redis地址: " + host + ":" + port + ":" + timeout);
            return jedisPool;
        } catch (Exception e) {
            log.error("初始化Redis连接池JedisPool异常:" + e.getMessage());
        }
        return null;
    }
}
