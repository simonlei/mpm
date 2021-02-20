package org.mpm.server.metas;

import lombok.extern.slf4j.Slf4j;
import org.nutz.boot.AppContext;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.util.NutMap;

@Slf4j
public class ConfigDataSource {

    public NutMap fetchConfig() {
        NutMap result = new NutMap();

        Ioc ioc = AppContext.getDefault().getIoc();
        log.info("IOC is {}", ioc);
        PropertiesProxy conf = ioc.getByType(PropertiesProxy.class);
        result.setv("thumbUrl", String.format("https://%s.cos.%s.myqcloud.com/",
                conf.get("cos.bucket"), conf.get("cos.region")));
        return result;
    }
}
