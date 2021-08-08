package org.mpm.server.pics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.progress.ProgressController;
import org.mpm.server.util.ExplicitPager;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PicsController {

    @Autowired
    Dao dao;

    @Autowired
    PicsService picsService;
    @Autowired
    TrashEmptyTask emptyTask;
    @Autowired
    GisService gisService;

    /*
    // used in client
    public void batchUpdatePics(boolean trashed, Map criteria, Map values) {
        String theYear = (String) criteria.get("theYear");
        String theMonth = (String) criteria.get("theMonth");
        String filePath = (String) criteria.get("filePath");
        if (values.get("longitude") != null || values.get("latitude") != null) {
            String address = getAddress(values);
            if (address != null) {
                values.put("address", address);
            }
        }

        if (filePath != null) {
            String s = "update t_photos inner join t_files on t_photos.id=t_files.photoId SET ";
            s += values.keySet().stream().map(k -> k + "=@" + k).collect(Collectors.joining(","));
            s += " where trashed=@isTrashed and t_files.path like @filePath";
            Sql sql = Sqls.create(s);
            for (Object k : values.keySet()) {
                sql.setParam((String) k, values.get(k));
            }
            sql.setParam("isTrashed", trashed).setParam("filePath", filePath + "%");
            dao.execute(sql);
        } else {
            Cnd cnd = Cnd.where("trashed", "=", trashed);
            cnd = theYear == null ? cnd : cnd.and("year(takenDate)", "=", theYear);
            cnd = theMonth == null ? cnd : cnd.and("month(takenDate)", "=", theMonth);
            dao.update(EntityPhoto.class, Chain.from(values), cnd);
        }
    }
     */

    // used in client
    @PostMapping("/api/getCount")
    public int count(@RequestBody boolean trashed) {
        return picsService.count(trashed);
    }

    @PostMapping("/api/emptyTrash")
    public String emptyTrash() {
        String taskId = ProgressController.addTask(emptyTask);
        new Thread(emptyTask).start();
        return taskId;
    }


    @PostMapping("/api/updateImage")
    public Map update(@RequestBody Map values) {
        Record record = new Record();
        record.putAll(values);

        if (values.get("longitude") != null || values.get("latitude") != null) {
            String address = getAddress(values);
            if (address != null) {
                record.set("address", address);
            }
        }
        dao.updateIgnoreNull(dao.getEntity(EntityPhoto.class).getObject(record));
        return addThumbField(dao.query("t_photos", Cnd.where("id", "=", record.getInt("id")))).get(0);
    }

    private String getAddress(Map values) {
        try {
            return gisService.getAddress(Double.parseDouble("" + values.get("latitude")),
                    Double.parseDouble("" + values.get("longitude")));
        } catch (Exception e) {
            log.error("Can't set address", e);
        }
        return null;
    }

    @PostMapping("/api/trashPhotos")
    public int trashPhotos(@RequestBody List<String> names) {
        Criteria cri = Cnd.cri();
        cri.where().andInStrList("name", names);
        return dao.update(EntityPhoto.class, Chain.makeSpecial("trashed", " !trashed"), cri);
    }

    @PostMapping("/api/getPics")
    public Map getPics(@RequestBody GetPicsRequest req) {
        log.info("Getting pics");
        Boolean trashed = req.trashed;

        Boolean star = null;
        String filePath = req.path;
        int start = req.getStart();
        int end = req.getStart() + req.getSize();
        String sortedBy = req.order;
        sortedBy = sortedBy == null ? "id" : sortedBy;
        boolean desc = sortedBy.startsWith("-");
        sortedBy = desc ? sortedBy.substring(1) : sortedBy;
        List<Record> photos;

        int totalRows;
        // TODO: 要重构
        if (Strings.isNotBlank(filePath)) {
            String joinSql = "inner join t_files on t_photos.id = t_files.photoId ";
            SimpleCriteria cnd = new SimpleCriteria(joinSql);
            cnd.where().and("t_files.path", "like", filePath + "%")
                    .and("trashed", "=", trashed);

            addStarCriteria(star, cnd);

            totalRows = (int) dao.fetch("t_photos", cnd, "count(distinct t_photos.id) as c").getLong("c");
            cnd.setPager(new ExplicitPager(start, end - start));
            cnd.orderBy(sortedBy, desc ? "desc" : "asc");
            // ...
            photos = dao.query("t_photos", cnd, null, "distinct t_photos.*");
        } else {
            Cnd cnd = Cnd.where("trashed", "=", trashed);
            cnd = addDateCondition(req, cnd);
            addStarCriteria(star, cnd.getCri());

            totalRows = dao.count(EntityPhoto.class, cnd);
            cnd.limit(new ExplicitPager(start, end - start));
            cnd.orderBy(sortedBy, desc ? "desc" : "asc");
            // ...
            photos = dao.query("t_photos", cnd);
        }

        return Lang.map("totalRows", totalRows).setv("startRow", start)
                .setv("endRow", start + photos.size()).setv("data", addThumbField(photos));
    }

    private Cnd addDateCondition(GetPicsRequest req, Cnd cnd) {
        Integer date = parseDate(req.dateKey); // 2021 或 202106
        if (date == null) {
            return cnd;
        }
        Integer theYear = date > 9999 ? date / 100 : date;
        Integer theMonth = date > 9999 ? date % 100 : null;
        cnd = theYear == null ? cnd : cnd.and("year(takenDate)", "=", theYear);
        cnd = theMonth == null ? cnd : cnd.and("month(takenDate)", "=", theMonth);
        return cnd;
    }

    private Integer parseDate(String dateKey) {
        try {
            return Integer.parseInt(dateKey);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Record> addThumbField(List<Record> photos) {
        for (Record r : photos) {
            r.put("thumb", getThumbUrl(r.getString("name"), r.getInt("rotate")));
            LocalDateTime takenDate = r.getTimestamp("takenDate").toLocalDateTime();
            r.put("theYear", takenDate.getYear());
            r.put("theMonth", takenDate.getMonthValue());
        }
        return photos;
    }

    private String getThumbUrl(String name, int rotate) {
        rotate = (360 + rotate) % 360;
        return "small/" + name + "/thumb" + rotate;
    }

    private void addStarCriteria(Boolean star, SimpleCriteria cnd) {
        if (star != null) {
            cnd.where().and("star", "=", star);
        }
    }

    @Data
    static class GetPicsRequest {

        boolean trashed;
        int start;
        int size;
        String order;
        String dateKey;
        String path;
    }
}