package org.mpm.server.cron;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityMeta;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.Static;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PhotoTaskScanner {

    @Autowired
    Dao dao;

    public void scanPhotoDoTask(String taskName, PhotoTask task, int pageSize, boolean ignoreError, Cnd cnd) {
        try {
            EntityMeta meta = dao.fetch(EntityMeta.class, taskName);
            long lastId = meta == null ? 0 : Long.parseLong(meta.getValue());
            List<EntityPhoto> photos = dao.query(EntityPhoto.class, Cnd.where("id", ">", lastId)
                    .and(cnd == null ? new Static("1=1") : cnd.where())
                    .orderBy("id", "asc"), new Pager(1, pageSize));
            for (EntityPhoto p : photos) {
                try {
                    task.dealPhoto(p);
                } catch (Exception e) {
                    if (ignoreError) {
                        log.error("Task {} can't deal with photo {}", taskName, p.getId());
                        lastId = p.getId();
                        saveMeta(taskName, meta, lastId);
                    } else {
                        throw e;
                    }
                }

            }
            log.info("{} {}", taskName, lastId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMeta(String taskName, EntityMeta meta, long lastId) {
        if (meta == null) {
            meta = EntityMeta.builder().key(taskName).build();
        }
        meta.setValue("" + lastId);
        dao.insertOrUpdate(meta);
    }
}
