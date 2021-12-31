package com.example.config;

import com.example.zk.ZookeeperConnect;
import com.example.zk.ZookeeperInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class CommonBeanConfig {

    @Bean
    public ZookeeperConnect zookeeperConnect(ZookeeperInfo zookeeperInfo) {
        return new ZookeeperConnect(zookeeperInfo);
    }


    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(16, 32, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000),
                r -> new Thread(r, "netty-rpc-" + "-" + r.hashCode()),
                new ThreadPoolExecutor.AbortPolicy());
        return serverHandlerPool;
    }
}
