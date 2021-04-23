package org.mpm.server.pics;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.util.MyUtils;
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
        Long parentId = (Long) req.getCriteria().get("parentId");
        Sql s = Sqls.create("select f.*, concat(f.name,'(',count(p.id),')') title from t_files f  "
                + " left join t_files fp on fp.path like concat(f.path, '%')"
                + " left join t_photos p  on fp.photoId=p.id"
                + " where f.isFolder=true and"
                + (parentId == null ? " f.parentId is null " : " f.parentId=@parentId")
                + " and p.trashed=@trashed group by f.id"
                + " order by "
                + (parentId == null ? " f.id" : " f.name"));
        s.setParam("trashed", trashed).setParam("parentId", parentId)
                .setCallback(Sqls.callback.records());
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
}
