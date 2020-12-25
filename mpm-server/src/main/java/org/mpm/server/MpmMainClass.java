package org.mpm.server;

import org.mpm.server.entity.EntityBlockPicture;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.boot.NbApp;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(create = "init")
public class MpmMainClass extends NbApp {

    @Inject
    Dao dao;

    public void init() {
        dao.create(EntityPhoto.class, false);
        dao.create(EntityFile.class, false);
        dao.create(EntityBlockPicture.class, false);
    }
}
