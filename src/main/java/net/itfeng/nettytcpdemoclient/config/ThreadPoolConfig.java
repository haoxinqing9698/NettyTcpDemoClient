package net.itfeng.nettytcpdemoclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author itfeng
 * @since 2024/1/9 19:05
 * 
 */
@Configuration
public class ThreadPoolConfig {
    @Bean(name = "messageHandlerPool")
    public Executor messageHandlerPool(@Value("${thread_pools.message_handler_pool.corePoolSize}") int corePoolSize,
                                         @Value("${thread_pools.message_handler_pool.maxPoolSize}") int maxPoolSize,
                                         @Value("${thread_pools.message_handler_pool.queueCapacity}") int queueCapacity,
                                         @Value("${thread_pools.message_handler_pool.threadNamePrefix}") String threadNamePrefix) {
        return initExecutor(corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);
    }

    @Bean(name = "messagePublisherPool")
    public Executor messagePublisherPool(@Value("${thread_pools.message_publisher_pool.corePoolSize}") int corePoolSize,
                                         @Value("${thread_pools.message_publisher_pool.maxPoolSize}") int maxPoolSize,
                                         @Value("${thread_pools.message_publisher_pool.queueCapacity}") int queueCapacity,
                                         @Value("${thread_pools.message_publisher_pool.threadNamePrefix}") String threadNamePrefix) {
        return initExecutor(corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);
    }

    private Executor initExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

}
