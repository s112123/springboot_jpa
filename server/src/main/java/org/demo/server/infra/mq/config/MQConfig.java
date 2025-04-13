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

    // post, chat, notice
    public static final String EXCHANGE_TOPIC = "ntf.exchange.topic";
    public static final String QUEUE_LIKE = "ntf.queue.like";
    public static final String ROUTING_LIKE = "ntf.like";
    public static final String QUEUE_FOLLOW = "ntf.queue.follow";
    public static final String ROUTING_FOLLOW = "ntf.follow";

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
}
