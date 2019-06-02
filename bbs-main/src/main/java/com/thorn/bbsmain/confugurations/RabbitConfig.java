package com.thorn.bbsmain.confugurations;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue unCheckedQueue() {
        return new Queue("unCheckedMsg");
    }

    @Bean
    public Queue newMsgAgainQueue() {
        return new Queue("newMsgAgain");
    }

    @Bean
    public Queue newMsgQueue() {
        return new Queue("newMsg");
    }

    @Bean
    public Queue newBroadcastQueue() {
        return new Queue("broadcast");
    }

    /**
     * 内容审核队列
     *
     * @return
     */
    @Bean
    public Queue reviewQueue() {
        return new Queue("review");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

}
