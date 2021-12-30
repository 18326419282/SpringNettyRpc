package com.example.netty;

import com.example.annotation.RPCProvider;
import com.example.core.service.RpcServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Data
public class NettyServer implements ApplicationContextAware {

    @Autowired
    private NettyInfo nettyInfo;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private Map<String, Object> serviceMap = new HashMap<>();

    public void start(){
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss,worker).channel(NioServerSocketChannel.class).childHandler(new RpcServerInitializer(serviceMap,threadPoolExecutor))
        .option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);
        bootstrap.bind(nettyInfo.getHost(), nettyInfo.getPort());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RPCProvider.class);
        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RPCProvider rpcProvider = serviceBean.getClass().getAnnotation(RPCProvider.class);
                serviceMap.put(rpcProvider.value().getName(), serviceBean);
            }
        }
    }
}
