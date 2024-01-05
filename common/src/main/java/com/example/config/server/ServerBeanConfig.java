package com.example.config.server;

import com.example.core.service.RPCServerManager;
import com.example.netty.NettyServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("'${rpc.type}'.contains('server')")
public class ServerBeanConfig {

    @Bean
    public NettyServer nettyServer(){
        return new NettyServer();
    }

    @Bean
    public RPCServerManager rpcServerManager(){
       return new RPCServerManager();
    }
}
