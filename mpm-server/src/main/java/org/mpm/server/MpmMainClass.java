package org.mpm.server;

import org.mpm.server.entity.EntityBlockPicture;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityMeta;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.pics.GeoChecker;
import org.nutz.boot.NbApp;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(create = "init", depose = "depose")
public class MpmMainClass extends NbApp {

    @Inject
    Dao dao;
    @Inject
    GeoChecker geoCheckTask;

    public void init() {
        dao.create(EntityPhoto.class, false);
        dao.create(EntityFile.class, false);
        dao.create(EntityBlockPicture.class, false);
        dao.create(EntityMeta.class, false);

        geoCheckTask.startTasks();
    }

    public void depose() {
        geoCheckTask.stopTasks();
    }
}
