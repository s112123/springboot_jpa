package org.demo.server.infra.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    /**
     * Redis DB 0번 연결을 위한 ConnectionFactory 빈 생성
     * 가입 인증 코드
     *
     * @return LettuceConnectionFactory
     */
    @Bean(name = "redisConnectionFactory00")
    @Primary
    public LettuceConnectionFactory connectionFactory00() {
        RedisStandaloneConfiguration factory = new RedisStandaloneConfiguration();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(0);
        return new LettuceConnectionFactory(factory);
    }

    /**
     * Redis DB 0번을 사용하는 RedisTemplate 빈 등록
     * 가입 인증 코드
     *
     * @param connectionFactory RedisTemplate 에서 사용한 ConnectionFactory
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplate00")
    @Primary
    public RedisTemplate<String, String> redisTemplate00(
            @Qualifier("redisConnectionFactory00") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis DB 1번 연결을 위한 ConnectionFactory 빈 생성
     * Refresh Token 저장
     *
     * @return LettuceConnectionFactory
     */
    @Bean(name = "redisConnectionFactory01")
    public LettuceConnectionFactory connectionFactory01() {
        RedisStandaloneConfiguration factory = new RedisStandaloneConfiguration();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(1);
        return new LettuceConnectionFactory(factory);
    }

    /**
     * Redis DB 1번을 사용하는 RedisTemplate 빈 등록
     * Refresh Token 저장
     *
     * @param connectionFactory RedisTemplate 에서 사용한 ConnectionFactory
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplate01")
    public RedisTemplate<String, String> redisTemplate01(
            @Qualifier("redisConnectionFactory01") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis DB 2번 연결을 위한 ConnectionFactory 빈 생성
     * 알림 메세지 저장
     *
     * @return LettuceConnectionFactory
     */
    @Bean(name = "redisConnectionFactory02")
    public LettuceConnectionFactory connectionFactory02() {
        RedisStandaloneConfiguration factory = new RedisStandaloneConfiguration();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(2);
        return new LettuceConnectionFactory(factory);
    }

    /**
     * Redis DB 2번을 사용하는 RedisTemplate 빈 등록
     * 알림 메세지 저장
     *
     * @param connectionFactory RedisTemplate 에서 사용한 ConnectionFactory
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplate02")
    public RedisTemplate<String, Object> redisTemplate02(
            @Qualifier("redisConnectionFactory02") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // LocalDateTime, LocalDate, ZonedDateTime 을 JSON 으로 직렬화하려면 필요하다
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Key 직렬화 → String
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);

        // Value 직렬화 → JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis DB 3번 연결을 위한 ConnectionFactory 빈 생성
     * 채팅 참여 방 번호 저장
     *
     * @return LettuceConnectionFactory
     */
    @Bean(name = "redisConnectionFactory03")
    public LettuceConnectionFactory connectionFactory03() {
        RedisStandaloneConfiguration factory = new RedisStandaloneConfiguration();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(3);
        return new LettuceConnectionFactory(factory);
    }

    /**
     * Redis DB 3번을 사용하는 RedisTemplate 빈 등록
     * 채팅 참여 방 번호 저장
     *
     * @param connectionFactory RedisTemplate 에서 사용한 ConnectionFactory
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplate03")
    public RedisTemplate<String, String> redisTemplate03(
            @Qualifier("redisConnectionFactory03") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
