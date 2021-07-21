package org.mpm.server;

import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan("org.mpm")
@EnableScheduling
@EnableAsync
@EnableFeignClients
@Slf4j
public class MpmApplication extends SpringBootServletInitializer {

    @Autowired
    Dao dao;

    public static void main(String[] args) {
        SpringApplication.run(MpmApplication.class, args);
    }
}
