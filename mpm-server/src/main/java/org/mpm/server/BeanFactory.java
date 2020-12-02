package org.mpm.server;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Configurable;
import org.nutz.lang.util.NutMap;

@IocBean
@Slf4j
public class BeanFactory implements Configurable {

    String secretId;
    String secretKey;
    String regionStr;

    @IocBean
    public COSClient getCosClient() {
        log.info(secretId + ":" + secretKey);
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region region = new Region(regionStr);
        ClientConfig clientConfig = new ClientConfig(region);
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }

    @Override
    public void setupProperties(NutMap conf) {
        this.secretId = conf.getString("cos.secretId");
        this.secretKey = conf.getString("cos.secretKey");
        this.regionStr = conf.getString("cos.region");
    }
}