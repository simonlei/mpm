package org.mpm.server.pics;

import org.mpm.server.entity.EntityPhotoTags;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.trans.Trans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TagsService {

    @Autowired
    Dao dao;

    public List<String> getAllTags() {
        Sql sql = Sqls.create("select distinct name from photo_tags");
        sql.setCallback(Sqls.callback.strList());
        dao.execute(sql);
        return sql.getList(String.class);
    }

    public void setTagsRelation(int id, String tags) {
        String[] tagList = Arrays.stream(tags.split(",")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        if (tagList.length == 0) {
            dao.clear("photo_tags", Cnd.where("photoId", "=", id));
            return;
        }
        Trans.exec(() -> {
            Sql sql = Sqls.create("delete from photo_tags where photoId=@id and name not in (@tagList)")
                    .setParam("id", id).setParam("tagList", tagList);
            dao.execute(sql);
            for (String tag : tagList) {
                if (dao.fetch(EntityPhotoTags.class, Cnd.where("photoId", "=", id).and("name", "=", tag)) == null) {
                    dao.insert(EntityPhotoTags.builder().name(tag).photoId((long) id).build(), true, false, false);
                }
            }
        });
    }
}
