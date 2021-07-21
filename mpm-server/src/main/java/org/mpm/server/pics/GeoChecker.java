package org.mpm.server.pics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityMeta;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.Exps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeoChecker {

    public static final int GEO_API_LIMIT = 8000;
    @Autowired
    Dao dao;
    @Autowired
    PicsService picsService;

    private int tokens = GEO_API_LIMIT;
    private ScheduledExecutorService executorService;

    public void startTasks() {
        // todo: enable scheduled
        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(() -> {
            log.info("Reset token from " + tokens);
            tokens = GEO_API_LIMIT; // reset token everyday
        }, 0, 1, TimeUnit.DAYS);
        executorService.scheduleWithFixedDelay(() -> {
            if (tokens > 0) {
                checkPhotoGeos();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void checkPhotoGeos() {
        EntityMeta meta = dao.fetch(EntityMeta.class, "lastCheckId");
        long lastId = meta == null ? 0 : Long.parseLong(meta.getValue());
        // get first 20
        List<EntityPhoto> photos = dao.query(EntityPhoto.class, Cnd.where("id", ">", lastId)
                .and(Exps.isNull("latitude"))
                .orderBy("id", "asc"), new Pager(1, 20));
        for (EntityPhoto p : photos) {
            try {
                checkPhoto(p);
            } catch (Exception e) {
                log.error("Can't check photo:" + p.getId(), e);
            }
            lastId = p.getId();
        }
        if (meta == null) {
            meta = EntityMeta.builder().key("lastCheckId").build();
        }
        meta.setValue("" + lastId);
        dao.insertOrUpdate(meta);
    }

    private void checkPhoto(EntityPhoto p) throws IOException {
        File tmpFile = null;
        try {
            // download photo
            tmpFile = File.createTempFile(p.getName(), "" + Math.random());
            picsService.saveCosFile(p.getName(), tmpFile);
            // check taken date and geo
            picsService.setDateFromExif(tmpFile, p);
            dao.updateIgnoreNull(p);
            if (p.getLatitude() != null) {
                tokens--;
                log.info("Update photo " + p.getId());
            }
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    public void stopTasks() {
        executorService.shutdown();
    }
}
