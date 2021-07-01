package org.mpm.server.pics;

import com.isomorphic.datasource.DSRequest;
import com.isomorphic.datasource.DSResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.util.ExplicitPager;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.lang.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PicsDataSource {

    @Autowired
    Dao dao;

    @Autowired
    PicsModule picsModule;
    @Autowired
    TrashEmptyTask emptyTask;

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

    // used in client
    public int count(boolean trashed) {
        return picsModule.count(trashed);
    }

    // used in client
    public String emptyTrash() {
        String taskId = ProgressDataSource.addTask(emptyTask);
        new Thread(emptyTask).start();
        return taskId;
    }

    // used in datasource
    public DSResponse update(DSRequest req) {
        DSResponse resp = new DSResponse();
        Record record = new Record();
        Map values = req.getValues();
        record.putAll(values);

        if (values.get("longitude") != null || values.get("latitude") != null) {
            String address = getAddress(values);
            if (address != null) {
                record.set("address", address);
            }
        }
        int affectedRows = dao.updateIgnoreNull(dao.getEntity(EntityPhoto.class).getObject(record));
        resp.setData(addThumbField(Lang.list(record)));
        resp.setAffectedRows(affectedRows);
        return resp;
    }

    private String getAddress(Map values) {
        try {
            return picsModule.getAddress(Double.parseDouble("" + values.get("latitude")),
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

    // used in datasource
    public DSResponse remove(DSRequest req) {
        DSResponse resp = new DSResponse();
        Long id = (Long) req.getCriteria().get("id");
        if (id != null) {
            Sql sql = Sqls.create("update t_photos set trashed = !trashed where id = @id");
            sql.setParam("id", id);
            dao.execute(sql);
            resp.setData(Lang.list(Lang.map("id", id)));
            resp.setAffectedRows(1);
        }
        log.info("remove : {}", req.getCriteria());
        return resp;
    }

    @PostMapping("/api/getPics")
    public Map getPics(@RequestBody GetPicsRequest request) {
        log.info("Getting pics");
        DSRequest req = new DSRequest();
        req.setCriteria(Lang.map("trashed", false));
        req.setStartRow(request.getStart());
        req.setEndRow(request.getStart() + request.getSize());
        DSResponse resp = fetch(req);

        return Lang.map("totalRows", resp.getTotalRows()).setv("startRow", resp.getStartRow())
                .setv("endRow", resp.getEndRow()).setv("data", resp.getData());
    }

    // used in datasource
    public DSResponse fetch(DSRequest req) {
        DSResponse resp = new DSResponse();
        Boolean trashed = (Boolean) req.getCriteria().get("trashed");

        Boolean star = (Boolean) req.getCriteria().get("star");
        String theYear = (String) req.getCriteria().get("theYear");
        String theMonth = (String) req.getCriteria().get("theMonth");
        String filePath = (String) req.getCriteria().get("filePath");
        int start = (int) req.getStartRow();
        int end = (int) req.getEndRow();
        String sortedBy = req.getSortBy();
        sortedBy = sortedBy == null ? "id" : sortedBy;
        boolean desc = sortedBy.startsWith("-");
        sortedBy = desc ? sortedBy.substring(1) : sortedBy;
        List<Record> photos;

        // TODO: 要重构
        if (filePath != null) {
            String joinSql = "inner join t_files on t_photos.id = t_files.photoId ";
            SimpleCriteria cnd = new SimpleCriteria(joinSql);
            cnd.where().and("t_files.path", "like", filePath + "%")
                    .and("trashed", "=", trashed);

            addStarCriteria(star, cnd);

            resp.setTotalRows(dao.fetch("t_photos", cnd, "count(distinct t_photos.id) as c").getLong("c"));
            cnd.setPager(new ExplicitPager(start, end - start));
            cnd.orderBy(sortedBy, desc ? "desc" : "asc");
            // ...
            photos = dao.query("t_photos", cnd, null, "distinct t_photos.*");
        } else {
            Cnd cnd = Cnd.where("trashed", "=", trashed);
            cnd = theYear == null ? cnd : cnd.and("year(takenDate)", "=", theYear);
            cnd = theMonth == null ? cnd : cnd.and("month(takenDate)", "=", theMonth);
            addStarCriteria(star, cnd.getCri());

            resp.setTotalRows(dao.count(EntityPhoto.class, cnd));
            cnd.limit(new ExplicitPager(start, end - start));
            cnd.orderBy(sortedBy, desc ? "desc" : "asc");
            // ...
            photos = dao.query("t_photos", cnd);
        }
        resp.setData(addThumbField(photos));
        resp.setStartRow(start);
        resp.setEndRow(start + photos.size());
        return resp;
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
        return name + "/thumb" + rotate;
    }

    private void addStarCriteria(Boolean star, SimpleCriteria cnd) {
        if (star != null) {
            cnd.where().and("star", "=", star);
        }
    }

    @Data
    static class GetPicsRequest {

        int start;
        int size;
    }
}