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

@IocBean
@Slf4j
public class PicsDateSource {

    // used in datasource
    public DSResponse fetch(DSRequest req) {
        DSResponse resp = new DSResponse();
        Dao dao = MyUtils.getByType(Dao.class);
        Boolean trashed = (Boolean) req.getCriteria().get("trashed");
        Boolean star = (Boolean) req.getCriteria().get("star");
        List<Record> photoDates = getPhotoDates(dao, trashed, star);
        NutMap lastYear = null;
        int yearCount = 0;
        List<NutMap> result = new ArrayList<>();
        result.add(Lang.map("id", "全部").setv("title", "全部"));
        for (Record r : photoDates) {
            if (lastYear == null) {
                lastYear = makeNewYear(result, r);
            }
            if (!Lang.equals(r.get("year"), lastYear.get("year"))) {
                lastYear.setv("title", lastYear.get("year") + "年(" + yearCount + ")");
                yearCount = 0;
                lastYear = makeNewYear(result, r);
            }
            NutMap month = Lang.map("id", lastYear.getInt("year") * 100 + r.getInt("month"))
                    .setv("parentId", lastYear.get("year"))
                    .setv("year", lastYear.get("year"))
                    .setv("month", r.get("month"))
                    .setv("photoCount", r.get("photoCount"))
                    .setv("title", r.get("month") + "月(" + r.get("photoCount") + ")");
            yearCount += r.getInt("photoCount", 0);
            result.add(month);
        }
        if (lastYear != null) {
            lastYear.setv("title", lastYear.get("year") + "(" + yearCount + ")");
        }
        resp.setData(result);
        return resp;
    }

    private NutMap makeNewYear(List<NutMap> result, Record r) {
        NutMap lastYear = Lang.map("id", r.get("year")).setv("year", r.get("year"))
                .setv("parentId", "全部");
        result.add(lastYear);
        return lastYear;
    }

    private List<Record> getPhotoDates(Dao dao, Boolean trashed, Boolean star) {
        // photo dates...include years and months
        Sql sql = Sqls.create("select year(takenDate) year, month(takenDate) month,"
                + " count(*) photoCount "
                + " from t_photos where trashed = " + trashed
                + (star == null ? "" : " and star = " + star)
                + " group by year, month order by year desc, month desc");
        sql.setCallback(Sqls.callback.records());
        dao.execute(sql);
        return sql.getList(Record.class);
    }
}
