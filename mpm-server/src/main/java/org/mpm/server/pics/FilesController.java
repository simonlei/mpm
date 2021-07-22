package org.mpm.server.pics;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.util.BusiException;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FilesController {

    @Autowired
    Dao dao;

    @PostMapping("/api/getFoldersData")
    public List<Record> getFoldersData(@RequestBody FoldersDataRequest req) {
        Long parentId = req.parentId;
        Sql s = Sqls.create("select f.id, f.path, concat(f.name,'(',count(distinct p.id),')') title from t_files f  "
                + " left join t_files fp on fp.path like concat(f.path, '%')"
                + " left join t_photos p  on fp.photoId=p.id"
                + " where f.isFolder=true and"
                + (parentId == null ? " f.parentId is null " : " f.parentId=@parentId")
                + " and p.trashed=@trashed "
                + (req.star == null ? "" : " and p.star=@star ")
                + " group by f.id"
                + " order by "
                + (parentId == null ? " f.id" : " f.name"));
        s.setParam("trashed", req.trashed).setParam("parentId", parentId);
        if (req.star != null) {
            s.setParam("star", req.star);
        }
        log.debug(s.toString());
        s.setCallback(Sqls.callback.records());
        dao.execute(s);
        List<Record> list = s.getList(Record.class);
        if (parentId == null) {
            for (Record r : list) {
                r.set("parentId", -1);
            }
        }
        return list;
    }

    @PostMapping("/api/switchTrashFolder")
    public int switchTrashFolder(@RequestBody SwitchTrashFolderSchema request) {
        String s = "update t_photos "
                + " inner join t_files on t_photos.id=t_files.photoId "
                + " SET trashed = @to"
                + " where t_files.path like @filePath";
        Sql sql = Sqls.create(s);
        sql.setParam("to", request.to);
        sql.setParam("filePath", request.path + "%");
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);

        return sql.getInt();
    }
    
/*
    // used in datasource
    public DSResponse update(DSRequest req) {
        DSResponse resp = new DSResponse();
        Record record = new Record();
        Map values = req.getValues();
        record.putAll(values);
        int affectedRows = dao.updateIgnoreNull(dao.getEntity(EntityFile.class).getObject(record));

        if (values.get("parentId") != null) {
            EntityFile node = dao.fetch(EntityFile.class, (Long) values.get("id"));
            EntityFile newParent = dao.fetch(EntityFile.class, (Long) values.get("parentId"));
            if ((Boolean) values.get("merge")) {
                mergeTo(node, newParent);
            } else {
                resetParentTo(node, newParent);
            }
        }

        resp.setData(dao.fetch("t_files", Cnd.where("id", "=", values.get("id"))));
        resp.setAffectedRows(affectedRows);
        return resp;
    }
*/

    public void resetParentTo(EntityFile child, EntityFile newParent) {
        if (newParent == null) {
            newParent = EntityFile.builder().path("/" + Math.random()).build();
        }
        log.info("Child {} parent id {}", child.getId(), newParent.getId());
        child.setParentId(newParent.getId());
        String newPath = newParent.getPath() + "/" + child.getName();
        log.info("New path {} {}", newPath, child.getParentId());
        child.setPath(newPath);
        dao.updateIgnoreNull(child);
        List<EntityFile> children = dao.query(EntityFile.class, Cnd.where("parentId", "=", child.getId()));
        for (EntityFile subChild : children) {
            resetParentTo(subChild, child);
        }
    }

    /**
     * 将node 下的所有目录和文件都转移到 newParent 目录下，并删除node
     * Used by dmi
     */
    public void mergeTo(EntityFile node, EntityFile newParent) {
        if (newParent == null) {
            throw new BusiException("不能合并到root下");
        }
        log.info("New parent id {}", newParent.getId());
        List<EntityFile> children = dao.query(EntityFile.class, Cnd.where("parentId", "=", node.getId()));
        for (EntityFile child : children) {
            resetParentTo(child, newParent);
        }
        dao.delete(node);
    }

    @Data
    static class FoldersDataRequest {

        Boolean trashed;
        Boolean star;
        Long parentId;
    }

    static class SwitchTrashFolderSchema {

        Boolean to;
        String path;
    }
}
