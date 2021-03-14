package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.filesystem.ProgressInterface;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

@IocBean(singleton = false)
public class TrashEmptyTask implements Runnable, ProgressInterface {

    @Inject
    Dao dao;
    @Inject
    PicsModule picsModule;
    int total = -1;
    int count = 0;

    @Override
    public NutMap getProgress() {
        return Lang.map("total", total).setv("count", count).setv("progress", calcProgress());
    }

    @Override
    public NutMap getFinishedProgress() {
        return Lang.map("count", 100).setv("total", 100).setv("progress", 100);
    }

    @Override
    public void run() {
        List<EntityPhoto> trashed = dao.query(EntityPhoto.class, Cnd.where("trashed", "=", true));
        total = trashed.size();
        for (EntityPhoto p : trashed) {
            picsModule.realDelete(p);
            count++;
        }
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public int getCount() {
        return count;
    }
}
