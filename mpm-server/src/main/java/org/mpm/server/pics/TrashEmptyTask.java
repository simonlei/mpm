package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.progress.AbstractProgressTask;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrashEmptyTask extends AbstractProgressTask implements Runnable {

    @Autowired
    Dao dao;
    @Autowired
    PicsService picsService;

    @Override
    public void run() {
        List<EntityPhoto> trashed = dao.query(EntityPhoto.class, Cnd.where("trashed", "=", true));
        setTotal(trashed.size());
        for (EntityPhoto p : trashed) {
            picsService.realDelete(p);
            countInc();
        }
    }
}
