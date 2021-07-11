package org.mpm.server.pics;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PicsDateSource {

    @Autowired
    Dao dao;

    @PostMapping("/api/getPicsDate")
    public List<NutMap> getPicsDate(@RequestBody PicDateRequest req) {
        NutMap lastYear = null;
        int yearCount = 0;
        List<NutMap> result = new ArrayList<>();
        List<Record> photoDates = getPhotoDates(req.getTrashed(), req.getStar());
        for (Record r : photoDates) {
            if (lastYear == null) {
                lastYear = Lang.map("id", r.get("year")).setv("months", Lang.list()).setv("year", r.get("year"));
                result.add(lastYear);
            }
            if (!Lang.equals(r.get("year"), lastYear.get("year"))) {
                lastYear.setv("title", lastYear.get("year") + "年(" + yearCount + ")");
                yearCount = 0;
                lastYear = Lang.map("id", r.get("year")).setv("months", Lang.list()).setv("year", r.get("year"));
                result.add(lastYear);
            }
            NutMap month = Lang.map("id", lastYear.getInt("year") * 100 + r.getInt("month"))
                    .setv("year", lastYear.get("year"))
                    .setv("month", r.get("month"))
                    .setv("photoCount", r.get("photoCount"))
                    .setv("title", r.get("month") + "月(" + r.get("photoCount") + ")");
            yearCount += r.getInt("photoCount", 0);
            lastYear.getAsList("months", NutMap.class).add(month);
        }
        if (lastYear != null) {
            lastYear.setv("title", lastYear.get("year") + "(" + yearCount + ")");
        }
        return result;
    }

    private List<Record> getPhotoDates(Boolean trashed, Boolean star) {
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

    @Data
    static class PicDateRequest {

        Boolean trashed;
        Boolean star;
    }
}
