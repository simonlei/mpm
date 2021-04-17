package org.mpm.server.metas;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class DataSources {

    @At("/datasources")
    @Ok("json")
    public String datasources() {
        return "";
    }
}
