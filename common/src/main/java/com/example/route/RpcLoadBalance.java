package com.example.route;

import com.example.core.RpcProtocol;
import com.example.core.client.RpcClientHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luxiaoxun on 2020-08-01.
 */
public abstract class RpcLoadBalance {
    // Service map: group by service name
    protected Map<String, List<RpcProtocol>> getServiceMap(Map<String, RpcClientHandler> connectedServerNodes, Map<String, RpcProtocol> rpcProtocolMap) {
        Map<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if (connectedServerNodes != null && connectedServerNodes.size() > 0) {
            for (String path : connectedServerNodes.keySet()) {
                RpcProtocol rpcProtocol = rpcProtocolMap.get(path);
                for (String serviceInfo : rpcProtocol.getServiceList()) {
                    List<RpcProtocol> rpcProtocolList = serviceMap.get(serviceInfo);
                    if (rpcProtocolList == null) {
                        rpcProtocolList = new ArrayList<>();
                    }
                    rpcProtocolList.add(rpcProtocol);
                    serviceMap.putIfAbsent(serviceInfo, rpcProtocolList);
                }
            }
        }
        return serviceMap;
    }

    // Route the connection for service key
    public abstract RpcProtocol route(String serviceInfo, Map<String, RpcClientHandler> connectedServerNodes,Map<String, RpcProtocol> rpcProtocolMap) throws Exception;
}
