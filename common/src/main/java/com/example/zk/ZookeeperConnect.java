package com.example.zk;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Data
public class ZookeeperConnect implements InitializingBean {

    private ZookeeperInfo zookeeperInfo;

    private CuratorClient curatorClient;

    public ZookeeperConnect(ZookeeperInfo zookeeperInfo) {
        this.zookeeperInfo = zookeeperInfo;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        curatorClient = new CuratorClient(getCuratorBuilder(zookeeperInfo));
        curatorClient.start();
    }


    public CuratorFrameworkFactory.Builder getCuratorBuilder(ZookeeperInfo zookeeperInfo) {
        return  getCuratorBuilder(zookeeperInfo.getConnetion(), zookeeperInfo.getZk_nameSpace(), zookeeperInfo.getZk_session_timeout(), zookeeperInfo.getZk_connect_timeout());

    }

        public CuratorFrameworkFactory.Builder getCuratorBuilder(String connectString, String namespace, int sessionTimeout, int connectionTimeout) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().namespace(namespace).connectString(connectString)
                .sessionTimeoutMs(sessionTimeout).connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10));
        return builder;
    }


    public List<String> getChildInfoString() throws Exception {
        List<String> infoData = new ArrayList<>();
        List<String> childPathList = curatorClient.getChildren(zookeeperInfo.getZk_registry_path());
        for (String childPath : childPathList) {
            infoData.add(new String(curatorClient.getData(zookeeperInfo.getZk_registry_path() + "/" + childPath)));
        }
        return infoData;

    }

    public List<String> getChildInfoString(String path) throws Exception {
        List<String> infoData = new ArrayList<>();
        List<String> childPathList = curatorClient.getChildren(path);
        for (String childPath : childPathList) {
            infoData.add(new String(curatorClient.getData(childPath)));
        }
        return infoData;
    }


    public String getInfoString(String path) throws Exception {
        return (new String(curatorClient.getData(path)));
    }


    public void pathListener(String path, BiConsumer<PathChildrenCacheEvent.Type, ChildData> biConsumer) throws Exception {
        curatorClient.pathListener(path, (curatorFramework, pathChildrenCacheEvent) -> {
            PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
            ChildData childData = pathChildrenCacheEvent.getData();
            biConsumer.accept(type, childData);
        });
    }


    public void addConnectionStateListener(BiConsumer<CuratorFramework, ConnectionState> biConsumer) throws Exception {
        curatorClient.addConnectionStateListener((framework, connectionState) -> {
            biConsumer.accept(framework, connectionState);
        });
    }


    public String createPathData(String path, byte[] data) throws Exception {
        return curatorClient.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }


    public void updatePathData(String path, byte[] data) throws Exception {
        curatorClient.setData().forPath(path, data);
    }

}
