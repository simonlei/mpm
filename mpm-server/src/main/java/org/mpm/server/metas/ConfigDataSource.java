package org.mpm.server.metas;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class ConfigDataSource {

    @Inject
    PropertiesProxy conf;

    @At("/meta/config")
    @Ok("json")
    public NutMap fetchConfig() {
        NutMap result = new NutMap();
        result.setv("thumbUrl", String.format("https://%s.cos.%s.myqcloud.com/",
                conf.get("cos.bucket"), conf.get("cos.region")));
        return result;
    }
}
