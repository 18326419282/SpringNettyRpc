package com.example.core.service;

import com.example.core.RpcProtocol;
import com.example.zk.ZookeeperConnect;
import com.example.zk.ZookeeperInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Data
@Slf4j
public class PRCServiceManage implements InitializingBean {

    @Autowired
    private ZookeeperConnect zookeeperConnect;

    @Autowired
    private ZookeeperInfo zookeeperInfo;


    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private HashSet<RpcProtocol> rpcProtocolSet = new HashSet<>();

    private volatile boolean isRunning = true;

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();

    @Override
    public void afterPropertiesSet() throws Exception {
        zookeeperConnect.pathListener(zookeeperInfo.getZk_registry_path(),
                (type, childData) -> {
                    try {
                        collectlistener(type, childData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void collectlistener(PathChildrenCacheEvent.Type type, ChildData childData) throws Exception {
        log.info("childPath:" + childData.getPath() + " ", type);
        switch (type) {
            case CONNECTION_RECONNECTED:
                getServiceAndUpdateServer();
                break;
            case CHILD_ADDED:
                log.info("Reconnected to zk, try to get latest service list");
                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
            case CHILD_UPDATED:
                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
            case CHILD_REMOVED:
                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                break;
        }
    }

    public void getServiceAndUpdateServer() throws Exception {
        List<RpcProtocol> rpcProtocolList = new ArrayList<>();
        List<String> serviceInfoList = zookeeperConnect.getChildInfoString();
        for (String serviceInfo : serviceInfoList) {
            RpcProtocol rpcProtocol = RpcProtocol.fromJson(serviceInfo);
            rpcProtocolList.add(rpcProtocol);
        }
        updateConnectedServer(rpcProtocolList);
    }


    public void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) throws Exception {
        String serviceInfo = zookeeperConnect.getInfoString(childData.getPath());
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(serviceInfo);
        updateConnectedServer(new ArrayList<RpcProtocol>() {{
            add(rpcProtocol);
        }});
    }

    public void updateConnectedServer(List<RpcProtocol> rpcProtocolList) {
        for (RpcProtocol rpcProtocol : rpcProtocolList) {
            System.out.println(rpcProtocol);
        }
    }
}


