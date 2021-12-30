package com.example.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcRequest implements Serializable {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
