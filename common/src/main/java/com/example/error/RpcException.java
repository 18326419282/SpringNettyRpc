package com.example.error;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcException extends RuntimeException{
    public RpcException(String message) {
        super(message);
    }
}
