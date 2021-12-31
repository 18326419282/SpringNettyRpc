package com.example;

import com.example.annotation.EnableRPCService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableRPCService
@SpringBootApplication
public class NettyClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyClientApplication.class,args);
    }
}
