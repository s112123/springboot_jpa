package org.demo.server.infra.mq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /**
     * SendAsyncService 에서 Post 를 전송할 때, 사용할 Executor 설정
     * 새 글은 많은 작성자가 동시에 글을 작성하고 팔로워들에게 메세지 알림을 보내므로 쓰레드를 10개 한다
     *
     * @return 쓰레드가 10개인 쓰레드 풀
     */
    @Bean(name = "sendPostTaskExecutor")
    public Executor taskExecutorForPost() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 기본 쓰레드 수
        executor.setCorePoolSize(10);
        // 최대 쓰레드 수
        executor.setMaxPoolSize(20);
        // 대기 큐 사이즈
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-SendMessage-Post-");
        executor.initialize();
        return executor;
    }

    /**
     * SendAsyncService 에서 Notice 를 전송할 때, 사용할 Executor 설정
     * 공지사항은 동시에 공지사항을 작성하는 경우가 많지 않기 때문에 쓰레드를 3개 한다
     *
     * @return 쓰레드가 3개인 쓰레드 풀
     */
    @Bean(name = "sendNoticeTaskExecutor")
    public Executor taskExecutorForNotice() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 기본 쓰레드 수
        executor.setCorePoolSize(3);
        // 최대 쓰레드 수
        executor.setMaxPoolSize(6);
        // 대기 큐 사이즈
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Async-SendMessage-Notice-");
        executor.initialize();
        return executor;
    }
}
