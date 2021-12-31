package com.example.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.netty.NettyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcProtocol {

    private NettyInfo nettyInfo;
    private List<String> ServiceList;


    public String toJson() {
        String json = JSON.toJSONString(this);
        return json;
    }

    public static RpcProtocol fromJson(String json) {
        return JSONObject.parseObject(json, RpcProtocol.class);
    }

}
