package org.mpm.server.activity;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityActivity;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ActivityController {

    @Autowired
    Dao dao;

    @PostMapping("/api/getActivities")
    public List<EntityActivity> getActivities() {
        return dao.query(EntityActivity.class, null);
    }

    @PostMapping("/api/createOrUpdateActivity")
    public int createOrUpdateActivity(@RequestBody ActivityParam param) {
        EntityActivity activity = param.activity;
        log.info("param {}", param);
        if (activity.getId() == null) {
            EntityActivity inserted = dao.insert(activity, true, false, false);
            EntityPhoto photo = dao.fetch(EntityPhoto.class, param.fromPhoto);
            if (photo != null) {
                // update photo
                photo.setActivity(inserted.getId());
                dao.update(photo);
            }
            return inserted.getId().intValue();
        } else {
            return dao.updateIgnoreNull(activity);
        }
    }

    @Data
    public static class ActivityParam {

        EntityActivity activity;
        Long fromPhoto;
    }
}
