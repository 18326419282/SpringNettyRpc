package com.example.proxy;

import com.example.core.client.RPCClientManager;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

@Data
public class NettyRPCFactoryBean implements FactoryBean {

    private Class<?> type;

    @Autowired
    private RPCClientManager rpcClientManager;

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new ObjectProxy(rpcClientManager));
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }
}
