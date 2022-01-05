package com.example.proxy;


import com.example.core.RpcFuture;
import com.example.core.RpcRequest;
import com.example.core.client.RPCClientManager;
import com.example.core.client.RpcClientHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy implements InvocationHandler {

    private RPCClientManager rpcClientManager;


    public ObjectProxy(RPCClientManager rpcClientManager) {
        this.rpcClientManager = rpcClientManager;
    }

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
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        RpcClientHandler handler = rpcClientManager.chooseHandler(method.getDeclaringClass().getName());
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }
}
