package com.example.route.impl;

import com.example.core.RpcProtocol;
import com.example.core.client.RpcClientHandler;
import com.example.route.RpcLoadBalance;
import com.google.common.hash.Hashing;

import java.util.List;
import java.util.Map;

public class RpcLoadBalanceConsistentHash extends RpcLoadBalance {

    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<String, RpcClientHandler> connectedServerNodes, Map<String, RpcProtocol> rpcProtocolMap) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes, rpcProtocolMap);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
