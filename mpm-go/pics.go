package main

import (
	"fmt"
	"mpm-go/model"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

func getCount(c *gin.Context) {
	var req GetPicsRequest
	err := c.BindJSON(&req)
	if err != nil {
		l.Info(err)
	}
	var count int64
	db().Table("t_photos").Where("trashed=?", req.Trashed).Count(&count)
	c.JSON(200, Response{0, count})
}

type GetPicsRequest struct {
	Star, Video, Trashed, IdOnly bool
	Start, FaceId, IdRank        int
	Size                         int `default:"75"`
	DateKey, Path, Tag           string
	Order                        string `default:"id"`
}

func getPics(c *gin.Context) {
	var req GetPicsRequest
	err := c.BindJSON(&req)
	if err != nil {
		l.Info("Can't bind request:", err)
		return
	}
	desc := strings.HasPrefix(req.Order, "-")
	if desc {
		req.Order = req.Order[1:]
	}
	joinSql := "left join t_activity on t_activity.id=t_photos.activity "
	var cnds []string
	params := make(map[string]interface{})
	cnds = append(cnds, "@a = 1")
	params["a"] = 1

	if req.Path != "" {
		joinSql += "inner join t_files on t_photos.id = t_files.photoId "
		cnds = append(cnds, "t_files.path like @filePath")
		params["filePath"] = req.Path + "%"
	}
	if req.FaceId > 0 {
		joinSql += "inner join photo_face_info f on t_photos.id=f.photoId "
		cnds = append(cnds, "f.faceId = @faceId")
		params["faceId"] = req.FaceId
	}
	cnds = append(cnds, "trashed = "+strconv.FormatBool(req.Trashed))
	if req.Star {
		cnds = append(cnds, "star = "+strconv.FormatBool(req.Star))
	}
	if req.Video {
		cnds = append(cnds, "video = "+strconv.FormatBool(req.Video))
	}
	if req.Tag != "" {
		cnds = append(cnds, "concat(',', tag, ',') like '%,"+req.Tag+",%'")
	}
	dateKey, _ := strconv.Atoi(req.DateKey)
	switch {
	case dateKey > 1000000:
		cnds = append(cnds, "activity = @activity")
		params["activity"] = dateKey - 1000000
	case dateKey > 9999: // 202405
		year := dateKey / 100
		month := dateKey % 100
		cnds = append(cnds, "year(taken_date)=@year and month(taken_date)=@month")
		params["year"] = year
		params["month"] = month
	case dateKey > 0: // 2024
		cnds = append(cnds, "year(taken_date)=@year")
		params["year"] = req.DateKey
	}
	if req.IdRank == 0 {
		total := 0
		db().Raw("select count(distinct t_photos.id) as c from t_photos "+joinSql+
			" where "+strings.Join(cnds, " and "), params).First(&total)
		l.Info("Total is ", total)
		sql := "select "
		if req.IdOnly {
			sql += "distinct t_photos.id"
		} else {
			sql += "distinct t_photos.*, " +
				"concat(t_activity.startDate, ' ', t_activity.name, ' ', t_activity.description) as activity_desc "
		}
		sql += " from t_photos " + joinSql + " where "
		sql += strings.Join(cnds, " and ")
		sql += " order by t_photos." + req.Order
		if desc {
			sql += " desc"
		}
		l.Info("sql is ", sql)
		if req.IdOnly {
			var results []map[string]interface{}

			tx := db().Raw(sql, params).Scan(&results)
			if tx.Error != nil {
				l.Info("getPics error", tx.Error)
			}
			c.JSON(200, Response{0, map[string]interface{}{
				"totalRows": total,
				"startRow":  req.Start,
				"endRow":    req.Start + len(results),
				"data":      results}})
		} else {
			sql += fmt.Sprintf(" limit %d, %d", req.Start, req.Size)
			l.Info("sql is ", sql)
			var results []*model.TPhoto

			tx := db().Raw(sql, params).Scan(&results)
			if tx.Error != nil {
				l.Info("getPics error", tx.Error)
			}
			results = addThumbField(results)
			c.JSON(200, Response{0, map[string]interface{}{
				"totalRows": total,
				"startRow":  req.Start,
				"endRow":    req.Start + len(results),
				"data":      results}})
		}
	} else {
		/*
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

		*/
	}
}

func addThumbField(records []*model.TPhoto) []*model.TPhoto {
	for _, r := range records {
		r.Thumb = getThumbUrl(r.Name, int64(r.Rotate))
		t := r.TakenDate
		r.TheYear = t.Year()
		r.TheMonth = t.Month()
	}
	return records
}

func getThumbUrl(name string, rotate int64) string {
	if rotate == 3600 {
		return "small/" + name + "/thumb"
	}
	rotate = (360 + rotate) % 360
	return fmt.Sprintf("small/%s/thumb%d", name, rotate)
}

func updateImage(c *gin.Context) {
	var values map[string]interface{}
	c.BindJSON(&values)
	result := make(map[string]any)
	for k, v := range values {
		result[k] = v
	}
	updatePhotoActivity(result, values)
	lo, loOk := values["longitude"]
	la, laOk := values["latitude"]
	if loOk && lo.(float64) > 0 || laOk && la.(float64) > 0 {
		address := getGisAddress(la.(float64), lo.(float64))
		if address != "" {
			result["address"] = address
		}
		if values["tags"] != nil {
			setTagsRelation(result["id"].(int), values["tags"].(string))
		}
	}
	db().Model(&model.TPhoto{}).Where("id=?", result["id"]).Updates(result)
	var p model.TPhoto
	db().First(&p, values["id"])
	c.JSON(200, Response{0, addThumbField([]*model.TPhoto{&p})[0]})
}

// 更改活动 id

func updatePhotoActivity(result, values map[string]any) {
	if _, ok := values["activity"]; !ok {
		return
	}
	var ac model.TActivity
	db().First(&ac, values["activity"])
	var photo model.TPhoto
	db().First(&photo, values["id"])
	if ac.ID == 0 || photo.ID == 0 {
		return
	}
	takeDate := time.Time(photo.TakenDate)
	startDate := time.Time(ac.StartDate)
	endDate := time.Time(ac.EndDate)
	if takeDate.Before(startDate) || takeDate.After(endDate.Add(24*time.Hour)) {
		// 如果当前时间不在活动范围内，更改时间为活动开始时间
		result["taken_date"] = startDate
	}
	if photo.Latitude == 0 && photo.Longitude == 0 {
		// 如果当前 GIS 是空，更改 GIS 为活动的 GIS
		result["longitude"] = ac.Longitude
		result["latitude"] = ac.Latitude
	}
}
