package org.mpm.server.pics;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;

@IocBean
@Slf4j
public class PicsDataSource {

    // used in client
    public int count(boolean trashed) {
        return MyUtils.getByType(PicsModule.class).count(trashed);
    }

    // used in datasource
    public DSResponse fetch(DSRequest req) {
        DSResponse resp = new DSResponse();
        Dao dao = MyUtils.getByType(Dao.class);
        List<Record> photoDates = getPhotoDates(dao, (Boolean) req.getCriteria().get("trashed"));
        NutMap lastYear = null;
        List<NutMap> result = new ArrayList<>();
        for (Record r : photoDates) {
            if (lastYear == null || !Lang.equals(r.get("theYear"), lastYear.get("title"))) {
                lastYear = Lang.map("title", r.get("theYear"));
                result.add(lastYear);
            }
            NutMap month = Lang.map("year", lastYear.get("title"))
                    .setv("month", r.get("theMonth"))
                    .setv("photoCount", r.get("photoCount"))
                    .setv("title", r.get("theMonth") + "(" + r.get("photoCount") + ")");
            result.add(month);
        }
        resp.setData(result);
        return resp;
    }

    private List<Record> getPhotoDates(Dao dao, Boolean trashed) {
        // photo dates...include years and months
        Sql sql = Sqls
                .create("select concat(y,'年') theYear, concat(m,'月') theMonth, photoCount"
                        + " from ( select year(takenDate) y, month(takenDate) m, count(*) photoCount "
                        + "        from t_photos where trashed = " + trashed
                        + " group by y, m order by y desc, m desc) x");
        sql.setCallback(Sqls.callback.records());
        dao.execute(sql);
        return sql.getList(Record.class);
    }

    // used in client
    public String emptyTrash() {
        TrashEmptyTask emptyTask = Mvcs.getIoc().get(TrashEmptyTask.class);
        String taskId = ProgressDataSource.addTask(emptyTask);
        new Thread(emptyTask).start();
        return taskId;
    }
}
