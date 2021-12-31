package com.example.service;

import com.example.annotation.RPCProvider;

@RPCProvider(HelloService.class)
public class HelloServiceImpl implements HelloService{

    @Override
    public String sayHello(String info) {
        return "hello! "+ info;
    }
}
