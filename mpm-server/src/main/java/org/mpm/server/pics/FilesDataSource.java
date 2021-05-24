package org.mpm.server.pics;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
@Slf4j
public class FilesDataSource {

    // used in datasource
    public DSResponse fetch(DSRequest req) {
        DSResponse resp = new DSResponse();
        Boolean trashed = (Boolean) req.getCriteria().get("trashed");
        Boolean star = (Boolean) req.getCriteria().get("star");
        Long parentId = (Long) req.getCriteria().get("parentId");
        Sql s = Sqls.create("select f.*, concat(f.name,'(',count(distinct p.id),')') title from t_files f  "
                + " left join t_files fp on fp.path like concat(f.path, '%')"
                + " left join t_photos p  on fp.photoId=p.id"
                + " where f.isFolder=true and"
                + (parentId == null ? " f.parentId is null " : " f.parentId=@parentId")
                + " and p.trashed=@trashed "
                + (star == null ? "" : " and p.star=@star ")
                + " group by f.id"
                + " order by "
                + (parentId == null ? " f.id" : " f.name"));
        s.setParam("trashed", trashed).setParam("parentId", parentId);
        if (star != null) {
            s.setParam("star", star);
        }
        s.setCallback(Sqls.callback.records());
        Dao dao = MyUtils.getByType(Dao.class);
        dao.execute(s);
        List<Record> list = s.getList(Record.class);
        if (parentId == null) {
            for (Record r : list) {
                r.set("parentId", -1);
            }
            list.add(0, new Record().set("title", "全部").set("id", -1));
        }
        resp.setData(list);
        return resp;
    }

    // used in datasource
    public DSResponse update(DSRequest req) {
        DSResponse resp = new DSResponse();
        Dao dao = MyUtils.getByType(Dao.class);
        Record record = new Record();
        Map values = req.getValues();
        record.putAll(values);
        String title = (String) values.get("title");
        int affectedRows = dao.updateIgnoreNull(dao.getEntity(EntityFile.class).getObject(record));

        if (values.get("parentId") != null) {
            resetParentTo(dao.fetch(EntityFile.class, (Long) values.get("id")),
                    dao.fetch(EntityFile.class, (Long) values.get("parentId")));
        }

        Record newData = dao.fetch("t_files", Cnd.where("id", "=", values.get("id")));
        newData.put("title", title);
        resp.setData(newData);
        resp.setAffectedRows(affectedRows);
        return resp;
    }

    public void resetParentTo(EntityFile child, EntityFile newParent) {
        if (newParent == null) {
            newParent = EntityFile.builder().path("/" + Math.random()).build();
        }
        child.setParentId(newParent.getId());
        String newPath = newParent.getPath() + "/" + child.getName();
        log.info("New path {}", newPath);
        child.setPath(newPath);
        Dao dao = MyUtils.getByType(Dao.class);
        dao.updateIgnoreNull(child);
        List<EntityFile> children = dao.query(EntityFile.class, Cnd.where("parentId", "=", child.getId()));
        for (EntityFile subChild : children) {
            resetParentTo(subChild, child);
        }
    }
}
