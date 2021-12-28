package com.exapmle.proxy;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

@Data
public class NettyRPCFactoryBean implements FactoryBean {

    private Class<?> type;


    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},new ObjectProxy());
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }
}
