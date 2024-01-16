package org.mpm.server.pics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.cron.PhotoTaskScanner;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.Exps;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeoChecker {

    @Autowired
    Dao dao;
    @Autowired
    PicsService picsService;
    @Autowired
    PhotoTaskScanner photoTaskScanner;

    public void checkPhotoGeos() {
        photoTaskScanner.scanPhotoDoTask("lastCheckId",
                this::checkPhoto, 20, true, Cnd.where("mediaType", "=", "photo").and(Exps.isNull("latitude")));
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
        photoTaskScanner.scanPhotoDoTask("lastDateCheckId", (p) -> {
            picsService.setInfosFromCos("origin/" + p.getName(), p);
            dao.updateIgnoreNull(p);
        }, 20, true, Cnd.where("mediaType", "=", "photo"));
    }

    public void generateSmallPhotos() {
        photoTaskScanner.scanPhotoDoTask("lastRegenerateSmallPhotosCheckId", (p) -> {
            picsService.generateSmallPic("origin/" + p.getName(), p.getName());
        }, 200, true, Cnd.where("mediaType", "=", "photo"));
    }

    public void clearDuplicateDescs() {
        photoTaskScanner.scanPhotoDoTask("lastDuplicateDescsCheckId", (p) -> {
            p.setDescription(removeDuplicate(p.getDescription()));
            dao.updateIgnoreNull(p);
        }, 200, true, null);
    }

    String removeDuplicate(String desc) {
        if (desc == null) {
            return "";
        }
        String[] strings = desc.split("\n");
        List<String> newDescList = new ArrayList<>();
        for (String s : strings) {
            s = s.trim();
            if (Strings.isBlank(s) || newDescList.contains(s)) {
                continue;
            }
            newDescList.add(s);
        }
        return String.join("\n", newDescList);
    }
}
