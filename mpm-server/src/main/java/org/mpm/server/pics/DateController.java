package org.mpm.server.pics;

import de.taimos.totp.TOTP;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DateController {

    @Autowired
    Dao dao;
    @Value("${totpSecretKey:}")
    String totpSecretKey;

    private static void addActivities(List<NutMap> activities, Map<Integer, TreeNode> monthMaps) {
        for (NutMap r : activities) {
            int monthId = r.getInt("year") * 100 + r.getInt("month");
            TreeNode month = monthMaps.get(monthId);
            if (month == null) {
                continue;
            }
            List<TreeNode> children = month.getChildren();
            if (children == null) {
                children = Lang.list();
                month.setChildren(children);
            }
            TreeNode activity = TreeNode.builder().id(1000000 + r.getInt("activityId")).year(month.getYear())
                    .month(month.getMonth()).photoCount(r.getInt("photoCount"))
                    .title(String.format("%d月%d日-%s(%d)", r.getInt("month"), r.getInt("day"), r.getString("name"),
                            r.getInt("photoCount")))
                    .build();
            children.add(activity);
        }
    }

    private static void addYearAndMonths(List<NutMap> photoDates, List<TreeNode> result,
            Map<Integer, TreeNode> monthMaps) {
        TreeNode lastYear = null;
        int yearCount = 0;

        for (NutMap r : photoDates) {
            // log.info("Record is {}", r);
            if (lastYear == null) {
                lastYear = TreeNode.builder().id(r.getInt("year")).children(Lang.list()).year(r.getInt("year")).build();
                result.add(lastYear);
            }
            if (!Lang.equals(r.getInt("year"), lastYear.getYear())) {
                lastYear.setTitle(lastYear.getYear() + "年(" + yearCount + ")");
                yearCount = 0;
                lastYear = TreeNode.builder().id(r.getInt("year")).children(Lang.list()).year(r.getInt("year")).build();
                result.add(lastYear);
            }
            TreeNode month = TreeNode.builder().id(lastYear.getYear() * 100 + r.getInt("month"))
                    .year(lastYear.getYear())
                    .month(r.getInt("month"))
                    .photoCount(r.getInt("photoCount"))
                    .title(r.get("month") + "月(" + r.get("photoCount") + ")").build();
            monthMaps.put(month.getId(), month);
            yearCount += r.getInt("photoCount", 0);
            lastYear.getChildren().add(month);
        }
        if (lastYear != null) {
            lastYear.setTitle(lastYear.getYear() + "年(" + yearCount + ")");
        }
    }

    @PostMapping("/api/getPicsDate")
    // @Migrated
    public List<TreeNode> getPicsDate(@RequestBody PicDateRequest req) {
        log.info("Req is {}", req);
        List<TreeNode> result = new ArrayList<>();
        List<NutMap> photoDates = getPhotoDates(req.getTrashed(), req.getStar());
        Map<Integer, TreeNode> monthMaps = new HashMap<>();
        addYearAndMonths(photoDates, result, monthMaps);
        List<NutMap> activities = getActivities(req.getTrashed(), req.getStar());
        addActivities(activities, monthMaps);
        return result;
    }

    private List<NutMap> getActivities(Boolean trashed, Boolean star) {
        Sql sql = Sqls.create("""
                select year(startDate) as year, month(startDate) as month, t_activity.id as activityId,
                    count(t_photos.id) as photoCount, day(startDate) as day, t_activity.name as name
                from t_activity
                left join t_photos on t_photos.activity=t_activity.id
                where trashed = @trashed $started
                group by year, month, activityId
                order by year desc, month desc, startDate;
                """);
        return fetchRecords(trashed, star, sql);
    }

    private List<NutMap> fetchRecords(Boolean trashed, Boolean star, Sql sql) {
        sql.setParam("trashed", trashed != null && trashed);
        sql.setVar("started", star == null ? "" : " and star = " + star);
        sql.setCallback(Sqls.callback.maps());
        dao.execute(sql);
        return sql.getList(NutMap.class);
    }

    private List<NutMap> getPhotoDates(Boolean trashed, Boolean star) {
        // photo dates...include years and months
        Sql sql = Sqls.create("""
                select year(taken_date) as year, month(taken_date) as month, count(*) as photoCount
                from t_photos
                where trashed = @trashed $started
                group by year, month
                order by year desc, month desc;
                """);
        return fetchRecords(trashed, star, sql);
    }

    @GetMapping("/totp")
    public String getTotp() {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(totpSecretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    @Data
    @Builder
    public static class TreeNode {

        int id;
        int year;
        int month;
        int photoCount;
        String title;
        List<TreeNode> children;
    }

    @Builder
    @Data
    static class PicDateRequest {

        Boolean trashed;
        Boolean star;
    }
}
