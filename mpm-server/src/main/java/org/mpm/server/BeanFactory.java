package org.mpm.server;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(create = "init")
@Slf4j
public class BeanFactory {

    @Inject
    PropertiesProxy conf;

    String secretId;
    String secretKey;
    String regionStr;

    @IocBean
    public COSClient getCosClient() {
        log.info("cosClient config:" + secretId + ":" + secretKey);
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region region = new Region(regionStr);
        ClientConfig clientConfig = new ClientConfig(region);
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }

    public void init() {
        log.info("Setups : " + conf);
        this.secretId = conf.get("cos.secretId");
        this.secretKey = conf.get("cos.secretKey");
        this.regionStr = conf.get("cos.region");
    }
}