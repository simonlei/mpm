package org.mpm.server.pics;

import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagsService {

    @Autowired
    Dao dao;

    public String getAllTags() {
        Sql sql = Sqls.create("select distinct name from photo_tags");
        sql.setCallback(Sqls.callback.strs());
        dao.execute(sql);
        return String.join(",", sql.getList(String.class));
    }
}
