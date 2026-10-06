package com.sentinelx.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.messaging.MessagingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange(MessagingProperties properties) {
        return new TopicExchange(properties.deadLetterExchange(), true, false);
    }

    @Bean
    public Queue detectionQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.queue())
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .withArgument("x-dead-letter-routing-key", properties.routingKey())
                .build();
    }

    @Bean
    public Queue deadLetterQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.deadLetterQueue()).build();
    }

    @Bean
    public Binding detectionBinding(Queue detectionQueue, TopicExchange eventsExchange,
                                   MessagingProperties properties) {
        return BindingBuilder.bind(detectionQueue).to(eventsExchange).with(properties.routingKey());
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange,
                                    MessagingProperties properties) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(properties.routingKey());
    }

    /**
     * Spring Boot AMQP autoconfiguration TỰ ĐỘNG tạo RabbitTemplate/AmqpTemplate cho ta, và tự
     * động "nhặt" bean MessageConverter này để gắn vào RabbitTemplate đó (chuẩn Spring Boot:
     * auto-configuration luôn ưu tiên bean người dùng tự khai báo nếu có). Vì vậy ta KHÔNG tự
     * định nghĩa bean rabbitTemplate/AmqpTemplate bằng tay -- làm vậy sẽ trùng tên với bean do
     * RabbitAutoConfiguration tạo sẵn, gây lỗi BeanDefinitionOverrideException (đã gặp ở lần chạy
     * trước), vì Spring Boot mặc định không cho phép ghi đè bean cùng tên.
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}