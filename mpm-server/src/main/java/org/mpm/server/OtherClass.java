package org.mpm.server;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class OtherClass {
    @Ok("raw")
    @At("/now")
    public long now() {
        return System.currentTimeMillis();
    }
}
