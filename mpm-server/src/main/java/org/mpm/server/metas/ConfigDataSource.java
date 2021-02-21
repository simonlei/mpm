package org.mpm.server.metas;

import lombok.extern.slf4j.Slf4j;
import org.mpm.server.util.MyUtils;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.util.NutMap;

@Slf4j
public class ConfigDataSource {

    public NutMap fetchConfig() {
        NutMap result = new NutMap();

        PropertiesProxy conf = MyUtils.getByType(PropertiesProxy.class);
        result.setv("thumbUrl", String.format("https://%s.cos.%s.myqcloud.com/",
                conf.get("cos.bucket"), conf.get("cos.region")));
        return result;
    }
}
