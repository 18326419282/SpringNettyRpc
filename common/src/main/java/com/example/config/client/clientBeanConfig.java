package com.example.config.client;

import com.example.core.client.RPCClientManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("'${rpc.type}'.contains('client')")
class clientBeanConfig {

    @Bean
    public RPCClientManager rpcClientManage(){
        return new RPCClientManager();
    }
}
