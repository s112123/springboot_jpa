package org.demo.server.infra.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    // chat
    public static final String EXCHANGE_TOPIC = "ntf.exchange.topic";
    public static final String QUEUE_LIKE = "ntf.queue.like";
    public static final String QUEUE_FOLLOW = "ntf.queue.follow";
    public static final String QUEUE_POST = "ntf.queue.post";
    public static final String QUEUE_NOTICE = "ntf.queue.notice";
    public static final String QUEUE_CHAT = "ntf.queue.chat";
    public static final String ROUTING_LIKE = "ntf.like";
    public static final String ROUTING_FOLLOW = "ntf.follow";
    public static final String ROUTING_POST = "ntf.post";
    public static final String ROUTING_NOTICE = "ntf.notice";
    public static final String ROUTING_CHAT = "ntf.chat";

    // MessageConverter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        // LocalDateTime, LocalDate, ZonedDateTime 을 JSON 으로 직렬화하려면 필요하다
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    // Topic Exchange
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_TOPIC)
                .durable(true)
                .build();
    }

    // Like Queue
    @Bean
    public Queue likeQueue() {
        return QueueBuilder
                .durable(QUEUE_LIKE)
                .build();
    }

    // Like Binding
    @Bean
    public Binding likeBinding() {
        return BindingBuilder
                .bind(likeQueue())
                .to(topicExchange())
                .with(ROUTING_LIKE);
    }

    // Follow Queue
    @Bean
    public Queue followQueue() {
        return QueueBuilder
                .durable(QUEUE_FOLLOW)
                .build();
    }

    // Follow Binding
    @Bean
    public Binding followBinding() {
        return BindingBuilder
                .bind(followQueue())
                .to(topicExchange())
                .with(ROUTING_FOLLOW);
    }

    // Post Queue
    @Bean
    public Queue postQueue() {
        return QueueBuilder
                .durable(QUEUE_POST)
                .build();
    }

    // Post Binding
    @Bean
    public Binding postBinding() {
        return BindingBuilder
                .bind(postQueue())
                .to(topicExchange())
                .with(ROUTING_POST);
    }

    // Notice Queue
    @Bean
    public Queue noticeQueue() {
        return QueueBuilder
                .durable(QUEUE_NOTICE)
                .build();
    }

    // Notice Binding
    @Bean
    public Binding noticeBinding() {
        return BindingBuilder
                .bind(noticeQueue())
                .to(topicExchange())
                .with(ROUTING_NOTICE);
    }

    // Chat Queue
    @Bean
    public Queue chatQueue() {
        return QueueBuilder
                .durable(QUEUE_CHAT)
                .build();
    }

    // Chat Binding
    @Bean
    public Binding chatBinding() {
        return BindingBuilder
                .bind(noticeQueue())
                .to(topicExchange())
                .with(ROUTING_CHAT);
    }
}
