package com.example.zk;

import com.example.netty.NettyInfo;
import com.example.util.JsonUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ZookeeperResponse {

    private NettyInfo nettyInfo;
    private List<String> ServiceList;

    public String toJson() {
        String json = JsonUtil.objectToJson(this);
        return json;
    }
}
