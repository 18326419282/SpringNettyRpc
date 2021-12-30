package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${spring.zookeeper.connetion}")
    private String connect;


    @GetMapping("/hi")
    public String sayHi(String name){
      return connect;
    }


}
