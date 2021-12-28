package com.exapmle.zk;

import lombok.Data;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
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
        curatorClient = new CuratorClient(zookeeperInfo);
    }


    public List<String> getChildInfoString() throws Exception {
        List<String> infoData = new ArrayList<>();
        List<String> childPathList =  curatorClient.getChildren(zookeeperInfo.getZk_registry_path());
        for(String childPath : childPathList){
            infoData.add(new String(curatorClient.getData(zookeeperInfo.getZk_registry_path()+"/"+childPath)));
        }
        return infoData;

    }

    public List<String> getChildInfoString(String path) throws Exception {
        List<String> infoData = new ArrayList<>();
        List<String> childPathList =  curatorClient.getChildren(path);
        for(String childPath : childPathList){
            infoData.add(new String(curatorClient.getData(childPath)));
        }
        return infoData;
    }


    public String getInfoString(String path) throws Exception {
        return (new String(curatorClient.getData(path)));
    }



    public void pathListener(String path,BiConsumer<PathChildrenCacheEvent.Type,ChildData> biConsumer) throws Exception {
        curatorClient.pathListener(path, (curatorFramework, pathChildrenCacheEvent) -> {
            PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
            ChildData childData = pathChildrenCacheEvent.getData();
            biConsumer.accept(type,childData);
        });
    }

}
