package org.mpm.server.pics;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityActivity;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.progress.ProgressController;
import org.mpm.server.util.DaoUtil;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @Autowired
    TagsService tagsService;

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

    @PostMapping("/api/getCount")
    public int count(@RequestBody Map map) {
        return picsService.count((Boolean) map.get("trashed"));
    }

    @PostMapping("/api/getParentPathsForPhoto")
    public List<EntityFile> getParentPathsForPhoto(@RequestBody Long photoId) {
        Sql sql = Sqls.create("Select * from t_files where id in "
                + "( select distinct parentId from t_files where photoId=@photoId)");
        sql.setParam("photoId", photoId).setEntity(dao.getEntity(EntityFile.class));
        sql.setCallback(Sqls.callback.entities());
        dao.execute(sql);
        return sql.getList(EntityFile.class);
    }

    /**
     * 返回 path 从root开始的id列表
     */
    @PostMapping("/api/getFolderPaths")
    public List<Long> getFolderPaths(@RequestBody Long pathId) {
        List<Long> paths = new ArrayList<>();
        paths.add(pathId);
        EntityFile entityFile = dao.fetch(EntityFile.class, pathId);
        while (entityFile.getParentId() != null) {
            paths.add(0, entityFile.getParentId());
            entityFile = dao.fetch(EntityFile.class, entityFile.getParentId());
        }
        return paths;
    }

    @PostMapping("/api/emptyTrash")
    public NutMap emptyTrash() {
        String taskId = ProgressController.addTask(emptyTask);
        new Thread(emptyTask).start();
        return NutMap.NEW().setv("taskId", taskId);
    }

    @PostMapping("/api/updateImage")
    public Map update(@RequestBody Map values) {
        Record record = new Record();
        updatePhotoActivity(record, values);
        record.putAll(values);

        if (values.get("longitude") != null || values.get("latitude") != null) {
            String address = getAddress(values);
            if (address != null) {
                record.set("address", address);
            }
        }
        if (values.get("tags") != null) {
            tagsService.setTagsRelation(record.getInt("id"), (String) values.get("tags"));
        }
        dao.updateIgnoreNull(dao.getEntity(EntityPhoto.class).getObject(record));
        return addThumbField(dao.query("t_photos", Cnd.where("id", "=", record.getInt("id")))).get(0);
    }

    /**
     * 更改活动 id
     */
    private void updatePhotoActivity(Record record, Map values) {
        if (values.get("activity") == null) {
            return;
        }
        EntityActivity fetched = dao.fetch(EntityActivity.class, (Integer) values.get("activity"));
        EntityPhoto photo = dao.fetch(EntityPhoto.class, (Integer) values.get("id"));
        if (fetched == null || photo == null) {
            return;
        }
        if (photo.getTakenDate().isBefore(fetched.getStartDate().atStartOfDay())
                || photo.getTakenDate().isAfter(fetched.getEndDate().atTime(23, 59, 59, 999))) {
            // 如果当前时间不在活动范围内，更改时间为活动开始时间
            record.set("takenDate", fetched.getStartDate().atStartOfDay());
        }
        if (photo.getLatitude() == null && photo.getLongitude() == null) {
            // 如果当前 GIS 是空，更改 GIS 为活动的 GIS
            record.set("longitude", fetched.getLongitude());
            record.set("latitude", fetched.getLatitude());
        }
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

    @PostMapping("/api/getPicIndex")
    public Long getPicIndex(@RequestBody GetPicIndexRequest req) {
        req.condition.setIdRank(req.picId);
        return MyUtils.parseLong("" + getPics(req.condition).get("r"), 1L) - 1;
    }

    @GetMapping("/geo_json_api/loadMarkersGeoJson")
    public NutMap loadMarkersGeoJson() {
        List<Record> markers = loadMarkers();
        List<NutMap> features = new ArrayList<>();
        for (Record r : markers) {
            NutMap feature = new NutMap();
            NutMap geometry = new NutMap().setv("type", "Point")
                    .setv("coordinates", Lang.list(r.getDouble("longitude"), r.getDouble("latitude")));
            feature.setv("type", "Feature").setv("properties", r).setv("geometry", geometry);
            features.add(feature);
        }
        return new NutMap().setv("type", "FeatureCollection").setv("features", features);
    }

    @PostMapping("/api/loadMarkers")
    public List loadMarkers() {
        Sql sql = Sqls.create("""
                        select id, name, latitude, longitude, rotate
                        from t_photos where latitude is not null and trashed=false
                        """)
                .setCallback(Sqls.callback.records());
        dao.execute(sql);
        return sql.getList(Record.class);
    }

    @PostMapping("/api/getPics")
    public Map getPics(@RequestBody GetPicsRequest req) {
        log.info("Getting pics " + Json.toJson(req));
        Boolean trashed = req.trashed != null && req.trashed;

        Boolean star = req.star != null && req.star;
        String filePath = req.path;
        req.idOnly = req.idOnly != null && req.idOnly;
        int start = req.getStart() == null ? 0 : req.getStart();
        int size = req.getSize() == null ? 75 : req.getSize();
        String sortedBy = req.order == null ? "id" : req.order;
        boolean desc = sortedBy.startsWith("-");
        sortedBy = desc ? sortedBy.substring(1) : sortedBy;

        Sql sql = Sqls.create("""
                select $fields from t_photos
                $join
                $condition
                $limit
                """);
        String joinSql = "";
        Cnd cnd = Cnd.where("trashed", "=", trashed);
        if (Strings.isNotBlank(filePath)) {
            joinSql = "inner join t_files on t_photos.id = t_files.photoId ";
            cnd.and("t_files.path", "like", filePath + "%");
        }
        if (req.faceId != null) {
            joinSql = "inner join photo_face_info f on t_photos.id=f.photoId ";
            cnd.and("f.faceId", "=", req.faceId);
        }
        sql.setVar("join", joinSql);
        addStarCriteria(star, cnd.getCri());
        addVideoCriteria(req.getVideo(), cnd.getCri());
        addTagCriteria(req.getTag(), cnd.getCri());
        cnd = addDateCondition(req, cnd);

        if (req.idRank == null) {
            sql.setVar("fields", "count(distinct t_photos.id) as c");
            Long totalRows = (Long) DaoUtil.fetchOne(dao, sql, "c");

            cnd.orderBy(sortedBy, desc ? "desc" : "asc");
            sql.setVar("fields", req.idOnly ? "distinct t_photos.id" : "distinct t_photos.*");
            sql.setCondition(cnd);
            sql.setVar("limit", req.idOnly ? "" : " limit " + start + ", " + size);
            log.info("sql is " + sql.toPreparedStatement());
            List<Record> records = DaoUtil.fetchRecords(dao, sql);

            return Lang.map("totalRows", totalRows).setv("startRow", start)
                    .setv("endRow", start + records.size()).setv("data", req.idOnly ? records : addThumbField(records));
        } else {
            sql = Sqls.create("""
                    select id, r from (
                        select distinct t_photos.id, rank() over (order by $sortedBy $desc) as r
                        from t_photos
                        $condition) t
                    where id=@id
                    """);
            sql.setVar("sortedBy", sortedBy).setVar("desc", desc ? "desc" : "asc")
                    .setParam("id", req.idRank).setCondition(cnd);
            sql.setCallback(Sqls.callback.map());
            log.info("Sql is " + sql);
            dao.execute(sql);
            return (Map) sql.getResult();
        }
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
        if (rotate == 3600) {
            return "small/" + name + "/thumb";
        }
        rotate = (360 + rotate) % 360;
        return "small/" + name + "/thumb" + rotate;
    }

    private void addStarCriteria(Boolean star, SimpleCriteria cnd) {
        if (star != null && star) {
            cnd.where().and("star", "=", true);
        }
    }

    private void addVideoCriteria(Boolean video, SimpleCriteria cnd) {
        if (video != null && video) {
            cnd.where().and("mediaType", "=", "video");
        }
    }

    private void addTagCriteria(String tag, SimpleCriteria cnd) {
        if (tag != null) {
            cnd.where().and(new Static(" concat(',',tags,',') like '%," + tag + ",%'"));
        }
    }

    @GetMapping("/api/fixPic/{picId}")
    public String fixPic(@PathVariable("picId") Long picId) {
        EntityPhoto photo = dao.fetch(EntityPhoto.class, picId);
        return picsService.fixPhoto(photo);
    }

    @Data
    static class GetPicIndexRequest {

        GetPicsRequest condition;
        Long picId;
    }

    @Data
    static class GetPicsRequest {

        Boolean star;
        Boolean video;
        Boolean trashed;
        Integer start;
        Integer size;
        String order;
        String dateKey;
        String path;
        Long idRank;
        String tag;
        Boolean idOnly;
        Integer faceId;
    }
}