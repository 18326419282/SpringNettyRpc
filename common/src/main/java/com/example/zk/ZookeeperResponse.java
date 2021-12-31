package com.example.zk;

import com.alibaba.fastjson.JSON;
import com.example.netty.NettyInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ZookeeperResponse {

    private NettyInfo nettyInfo;
    private List<String> ServiceList;

    public String toJson() {
        String json = JSON.toJSONString(this);
        return json;
    }
}
