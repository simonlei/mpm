package org.mpm.server;

import org.nutz.boot.NbApp;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class MpmMainClass extends NbApp {
    @At("/xxx")
    @Ok("->:/index.html")
    public void index() {}


}
