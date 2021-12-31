package com.example.service;

import com.example.annotation.RPCService;

@RPCService
public interface HelloService {
    public String sayHello(String info);
}
