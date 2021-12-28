package com.exapmle.zk;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "demo2.zookeeper")
public class ZookeeperInfo implements InitializingBean {

    private String connetion;

    private int zk_session_timeout;

    private int zk_connect_timeout;

    private String zk_registry_path;

    private String zk_data_path;

    private String zk_real_date_path;

    private String zk_nameSpace;

    @Override
    public void afterPropertiesSet() throws Exception {
        zk_real_date_path = zk_registry_path + zk_data_path;
    }
}
