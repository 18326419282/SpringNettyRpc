package com.example.core.client;

import com.example.core.RpcProtocol;
import com.example.route.RpcLoadBalance;
import com.example.zk.ZookeeperConnect;
import com.example.zk.ZookeeperInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Slf4j
public class RPCClientManager implements InitializingBean, DisposableBean {

    @Autowired
    private ZookeeperConnect zookeeperConnect;

    @Autowired
    private ZookeeperInfo zookeeperInfo;


    @Autowired
    private RpcLoadBalance loadBalance;

    private Map<String, RpcProtocol> rpcProtocolMap = new HashMap<>();

    private volatile boolean isRunning = true;

    private long waitTimeout = 5000;

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    private Map<String, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {
        addOrUpdateConnectedServer(getServiceAndUpdateServer());
        zookeeperConnect.pathListener(zookeeperInfo.getZk_registry_path(),
                (type, childData) -> {
                    try {
                        collectlistener(type, childData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        if (connectedServerNodes.size() > 0) {
            signalAvailableHandler();
        }
    }


    public void collectlistener(PathChildrenCacheEvent.Type type, ChildData childData) throws Exception {
        switch (type) {
            case CONNECTION_RECONNECTED:
                addOrUpdateConnectedServer(getServiceAndUpdateServer());
                break;
            case CHILD_ADDED:
                log.info("Reconnected to zk, try to get latest service list");
                addOrUpdateConnectedServer(getServiceAndUpdateServer(childData));
                break;
            case CHILD_UPDATED:
                addOrUpdateConnectedServer(getServiceAndUpdateServer(childData));
                break;
            case CHILD_REMOVED:
                removeConnectedServer(childData.getPath());
                break;
            default:
                break;
        }
    }

    public List<RpcProtocol> getServiceAndUpdateServer() throws Exception {
        List<RpcProtocol> rpcProtocolList = new ArrayList<>();
        Map<String, String> serviceInfoMap = zookeeperConnect.getChildInfoString();
        for (Map.Entry<String, String> serviceInfo : serviceInfoMap.entrySet()) {
            RpcProtocol rpcProtocol = RpcProtocol.fromJson(serviceInfo.getValue());
            rpcProtocol.setPath(serviceInfo.getKey());
            rpcProtocolList.add(rpcProtocol);
        }
        return rpcProtocolList;
    }


    public List<RpcProtocol> getServiceAndUpdateServer(ChildData childData) throws Exception {
        String serviceInfo = zookeeperConnect.getInfoString(childData.getPath());
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(serviceInfo);
        rpcProtocol.setPath(childData.getPath());
        return new ArrayList<RpcProtocol>() {{
            add(rpcProtocol);
        }};
    }

    public void addOrUpdateConnectedServer(List<RpcProtocol> rpcProtocolList) {
        for (RpcProtocol rpcProtocol : rpcProtocolList) {
            // Now using 2 collections to manage the service info and TCP connections because making the connection is async
            // Once service info is updated on ZK, will trigger this function
            // Actually client should only care about the service it is using
            if (!rpcProtocolMap.keySet().contains(rpcProtocol.getPath())) {
                connectServerNode(rpcProtocol);
            } else if (!rpcProtocolMap.get(rpcProtocol.getPath()).getNettyInfo().equals(rpcProtocol.getNettyInfo())) {
                removeConnectedServer(rpcProtocol.getPath());
                connectServerNode(rpcProtocol);
            } else {
                updateHandlerServer(rpcProtocol);
            }
        }
    }

    private void connectServerNode(RpcProtocol rpcProtocol) {
        if (CollectionUtils.isEmpty(rpcProtocol.getServiceList())) {
            log.info("No service on node, host: {}, port: {}", rpcProtocol.getNettyInfo().getHost(), rpcProtocol.getNettyInfo().getPort());
            return;
        }
        rpcProtocolMap.put(rpcProtocol.getPath(), rpcProtocol);
        log.info("New service node, host: {}, port: {}", rpcProtocol.getNettyInfo().getHost(), rpcProtocol.getNettyInfo().getPort());
        for (String serviceInfo : rpcProtocol.getServiceList()) {
            log.info("New service info, name: {}, version: {}", serviceInfo);
        }
        io.netty.bootstrap.Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RpcClientInitializer());
        ChannelFuture channelFuture = b.connect(rpcProtocol.getNettyInfo().getHost(), rpcProtocol.getNettyInfo().getPort());
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    log.info("Successfully connect to remote server, remote peer = " + rpcProtocol.getNettyInfo().getHost() + ":" + rpcProtocol.getNettyInfo().getPort());
                    RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                    connectedServerNodes.put(rpcProtocol.getPath(), handler);
//                            signalAvailableHandler();
                } else {
                    log.error("Can not connect to remote server, remote peer = " + rpcProtocol.getNettyInfo().getHost() + ":" + rpcProtocol.getNettyInfo().getPort());
                }
            }
        });
    }

    public void updateHandlerServer(RpcProtocol rpcProtocol) {
        rpcProtocolMap.put(rpcProtocol.getPath(),rpcProtocol);
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public RpcClientHandler chooseHandler(String serviceName) throws Exception {
        int size = connectedServerNodes.values().size();
        if(size <= 0){
            waitingForHandler();
        }
        while (isRunning && size > 0) {
            RpcProtocol rpcProtocol = loadBalance.route(serviceName, connectedServerNodes, rpcProtocolMap);
            RpcClientHandler handler = connectedServerNodes.get(rpcProtocol.getPath());
            return handler;
        }
        log.error("No server provide");
        throw new RuntimeException("No server provide");
    }


    public void removeConnectedServer(String path) {
        RpcClientHandler handler = connectedServerNodes.get(path);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(path);
        rpcProtocolMap.remove(path);
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void destroy() throws Exception {
        zookeeperConnect.stop();
        isRunning = false;
        for (String path : rpcProtocolMap.keySet()) {
            removeConnectedServer(path);
        }
        signalAvailableHandler();
        eventLoopGroup.shutdownGracefully();
    }
}


