package org.mpm.server;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

//@Component
public class MyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Value("${isDev}")
    String isDev;

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        // server.setPort(9000);
        if ("true".equals(isDev)) {
            server.setDocumentRoot(new File("target/gwt/launcherDir"));
        }
    }

}