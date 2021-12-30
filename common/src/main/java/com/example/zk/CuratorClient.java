package com.example.zk;

import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

public class CuratorClient extends CuratorFrameworkImpl {

    public CuratorClient(CuratorFrameworkFactory.Builder builder) {
        super(builder);
    }

    public byte[] getData(String path) throws Exception {
        return getData().forPath(path);
    }

    public List<String> getChildren(String path) throws Exception {
        return getChildren().forPath(path);
    }

    public void pathListener(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(this, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
        getConnectionStateListenable().addListener(connectionStateListener);
    }

}
