package com.example.proxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ObjectProxy implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if (name.equals("equals")) {
                return proxy == args[0];
            } else if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            } else if (name.equals("toString")) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        return null;
    }
}
