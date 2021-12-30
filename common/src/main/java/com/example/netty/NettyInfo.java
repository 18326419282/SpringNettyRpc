package com.example.netty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rpc.netty")
public class NettyInfo {

    private String host;
    private int port;

}
