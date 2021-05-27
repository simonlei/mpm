package org.mpm.server;

import com.isomorphic.base.InitListener;
import com.isomorphic.servlet.DataSourceLoader;
import com.isomorphic.servlet.IDACall;
import javax.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan("org.mpm")
@EnableScheduling
@EnableAsync
@Slf4j
public class MpmApplication extends SpringBootServletInitializer {

    @Autowired
    Dao dao;

    public static void main(String[] args) {
        //ISCInit.go();
        SpringApplication.run(MpmApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MpmApplication.class);
    }

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> customListenerBean() {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean();
        bean.setListener(new InitListener());
        return bean;
    }

    @Bean
    public ServletRegistrationBean customServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new IDACall(), "/app/sc/IDACall/*");
        return bean;
    }

    @Bean
    public ServletRegistrationBean customServletBean2() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new DataSourceLoader(), "/app/sc/DataSourceLoader");
        return bean;
    }
}
