package com.example.netty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Data
@Component
@ConfigurationProperties(prefix = "rpc.netty")
public class NettyInfo {

    private String host;
    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyInfo nettyInfo = (NettyInfo) o;
        return port == nettyInfo.port && Objects.equals(host, nettyInfo.host);
    }

}
