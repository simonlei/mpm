package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.progress.AbstractProgressTask;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(singleton = false)
public class TrashEmptyTask extends AbstractProgressTask implements Runnable {

    @Inject
    Dao dao;
    @Inject
    PicsModule picsModule;

    @Override
    public void run() {
        List<EntityPhoto> trashed = dao.query(EntityPhoto.class, Cnd.where("trashed", "=", true));
        setTotal(trashed.size());
        for (EntityPhoto p : trashed) {
            picsModule.realDelete(p);
            countInc();
        }
    }
}
