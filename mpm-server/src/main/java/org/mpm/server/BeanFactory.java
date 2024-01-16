package org.mpm.server;

import com.alibaba.druid.pool.DruidDataSource;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BeanFactory {

    @Value("${cos.bucket}")
    String bucket;
    @Value("${cos.region}")
    String region;
    @Value("${cos.secretId}")
    String secretId;
    @Value("${cos.secretKey}")
    String secretKey;

    @Value("${jdbc.url}")
    String dbUrl;
    @Value("${jdbc.username}")
    String dbUsername;
    @Value("${jdbc.password}")
    String dbPassword;

    @Bean
    public COSClient getCosClient() {
        log.info("cosClient config:" + secretId + ":" + secretKey);
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));

        return new COSClient(cred, clientConfig);
    }

    @Bean
    public IaiClient getIaiClient() {
        Credential credential = new Credential(secretId, secretKey);
        return new IaiClient(credential, region);
    }

    @Bean
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dbUrl);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean
    public Dao getDao() {
        return new NutDao(getDataSource());
    }
}