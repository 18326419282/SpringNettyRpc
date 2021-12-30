package com.example.core;

import com.example.netty.NettyInfo;
import com.example.util.JsonUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RpcProtocol {

    private NettyInfo nettyInfo;
    private List<String> ServiceList;


    public String toJson() {
        String json = JsonUtil.objectToJson(this);
        return json;
    }

    public static RpcProtocol fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcProtocol.class);
    }

}
