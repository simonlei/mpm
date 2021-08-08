package org.mpm.server.pics;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @Autowired
    Dao dao;
    @Autowired
    PicsService picsService;

    public void checkPhotoGeos() {
        EntityMeta meta = dao.fetch(EntityMeta.class, "lastCheckId");
        long lastId = meta == null ? 0 : Long.parseLong(meta.getValue());
        // get first 20
        List<EntityPhoto> photos = dao.query(EntityPhoto.class, Cnd.where("id", ">", lastId)
                .and(Exps.isNull("latitude")).and("mediaType", "=", "photo")
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
            picsService.saveCosFile("origin/" + p.getName(), tmpFile);
            // check taken date and geo
            picsService.setInfosFromCos("origin/" + p.getName(), p);
            picsService.setInfosFromFile(tmpFile, p);
            dao.updateIgnoreNull(p);
            if (p.getLatitude() != null) {
                log.info("Update photo " + p.getId());
            }
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    public void checkPhotoDates() {
        EntityMeta meta = dao.fetch(EntityMeta.class, "lastDateCheckId");
        long lastId = meta == null ? 0 : Long.parseLong(meta.getValue());
        // get first 20
        List<EntityPhoto> photos = dao.query(EntityPhoto.class, Cnd.where("id", ">", lastId)
                .and("mediaType", "=", "photo")
                .orderBy("id", "asc"), new Pager(1, 20));
        for (EntityPhoto p : photos) {
            try {
                picsService.setInfosFromCos("origin/" + p.getName(), p);
            } catch (Exception e) {
                log.error("Can't check photo date:" + p.getId(), e);
            }
            lastId = p.getId();
        }
        if (meta == null) {
            meta = EntityMeta.builder().key("lastDateCheckId").build();
        }
        meta.setValue("" + lastId);
        dao.insertOrUpdate(meta);
    }
}
