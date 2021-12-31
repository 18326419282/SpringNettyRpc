package com.example.core.service;

import com.example.core.RpcProtocol;
import com.example.netty.NettyInfo;
import com.example.netty.NettyServer;
import com.example.zk.ZookeeperConnect;
import com.example.zk.ZookeeperInfo;
import com.example.zk.ZookeeperResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Data
@Slf4j
public class RPCServerManager implements InitializingBean {

    @Autowired
    private ZookeeperConnect zookeeperConnect;

    @Autowired
    private NettyInfo nettyInfo;

    @Autowired
    private NettyServer nettyServer;


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
        nettyServer.start();
        registService();
        zookeeperConnect.addConnectionStateListener(this::stateChanged);
    }

    public void registService() {
        try {
            ZookeeperResponse zookeeperResponse = ZookeeperResponse.builder().nettyInfo(nettyInfo).ServiceList(nettyServer.getServiceMap().keySet().stream().collect(Collectors.toList())).build();
            zookeeperConnect.createPathData(getzkPath(String.valueOf(zookeeperResponse.hashCode())), zookeeperResponse.toJson().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("注册时发生问题:"+e);
        }
    }

    public String getzkPath(String path){
       return zookeeperInfo.getZk_real_date_path()+"-"+ path;
    }

    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        if (connectionState == ConnectionState.RECONNECTED) {
            log.info("Connection state: {}, register service after reconnected", connectionState);
            try {
                ZookeeperResponse zookeeperResponse = ZookeeperResponse.builder().nettyInfo(nettyInfo).ServiceList(nettyServer.getServiceMap().keySet().stream().collect(Collectors.toList())).build();
                zookeeperConnect.createPathData(getzkPath(String.valueOf(zookeeperResponse.hashCode())), zookeeperResponse.toJson().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("注册时发生问题:" + e);
            }
        }
    }


}


