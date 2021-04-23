package org.mpm.server.pics;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.util.ExplicitPager;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.mvc.Mvcs;

@IocBean
@Slf4j
public class PicsDataSource {

    // used in client
    public int count(boolean trashed) {
        return MyUtils.getByType(PicsModule.class).count(trashed);
    }

    // used in client
    public String emptyTrash() {
        TrashEmptyTask emptyTask = Mvcs.getIoc().get(TrashEmptyTask.class);
        String taskId = ProgressDataSource.addTask(emptyTask);
        new Thread(emptyTask).start();
        return taskId;
    }

    // used in datasource
    public DSResponse remove(DSRequest req) {
        DSResponse resp = new DSResponse();
        Long id = (Long) req.getCriteria().get("id");
        if (id != null) {
            Dao dao = MyUtils.getByType(Dao.class);
            Sql sql = Sqls.create("update t_photos set trashed = !trashed where id = @id");
            sql.setParam("id", id);
            dao.execute(sql);
            resp.setData(Lang.list(Lang.map("id", id)));
            resp.setAffectedRows(1);
        }
        log.info("remove : {}", req.getCriteria());
        return resp;
    }

    public DSResponse fetch(DSRequest req) {
        DSResponse resp = new DSResponse();
        Dao dao = MyUtils.getByType(Dao.class);
        Boolean trashed = (Boolean) req.getCriteria().get("trashed");
        String theYear = (String) req.getCriteria().get("theYear");
        String theMonth = (String) req.getCriteria().get("theMonth");
        String filePath = (String) req.getCriteria().get("filePath");
        int start = (int) req.getStartRow();
        int end = (int) req.getEndRow();
        if (filePath != null) {
            String joinSql = "inner join t_files on t_photos.id = t_files.photoId ";
            SimpleCriteria cnd = new SimpleCriteria(joinSql);
            cnd.where().and("t_files.path", "like", filePath + "%")
                    .and("trashed", "=", trashed);
            resp.setTotalRows(dao.count(EntityPhoto.class, cnd));
            cnd.setPager(new ExplicitPager(start, end - start));
            resp.setData(dao.query(EntityPhoto.class, cnd));
        } else {
            Cnd cnd = Cnd.where("trashed", "=", trashed);
            cnd = theYear == null ? cnd : cnd.and("year(takenDate)", "=", theYear);
            cnd = theMonth == null ? cnd : cnd.and("month(takenDate)", "=", theMonth);
            resp.setTotalRows(dao.count(EntityPhoto.class, cnd));
            cnd.limit(new ExplicitPager(start, end - start));
            resp.setData(dao.query(EntityPhoto.class, cnd));
        }
        return resp;
    }
}