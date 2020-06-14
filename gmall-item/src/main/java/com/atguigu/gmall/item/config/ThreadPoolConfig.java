package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPoll.corePoolSize}") Integer coreSize,
            @Value("${threadPoll.maximumPoolSize}") Integer maxSize,
            @Value("${threadPoll.keepAliveTime}") Integer keepalive,
            @Value("${threadPoll.workQueue}") Integer blockQueueSize) {

        return new ThreadPoolExecutor(coreSize, maxSize, keepalive, TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockQueueSize));
    }
}