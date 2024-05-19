package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"log"
	"strconv"
	"strings"
	"time"
)

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
		log.Println("Can't bind request:", err)
		return
	}
	desc := strings.HasPrefix(req.Order, "-")
	if desc {
		req.Order = req.Order[1:]
	}
	joinSql := "left join t_activity on t_activity.id=t_photos.activity "
	var cnds []string
	params := make(map[string]interface{})

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
		cnds = append(cnds, "year(takenDate)=@year and month(takenDate)=@month")
		params["year"] = year
		params["month"] = month
	case dateKey > 0: // 2024
		cnds = append(cnds, "year(takenDate)=@year")
		params["year"] = req.DateKey
	}
	if req.IdRank == 0 {
		total := 0
		db().Raw("select count(distinct t_photos.id) as c from t_photos "+joinSql+
			" where "+strings.Join(cnds, " and "), params).First(&total)
		log.Println("Total is ", total)
		sql := "select "
		if req.IdOnly {
			sql += "distinct t_photos.id"
		} else {
			sql += "distinct t_photos.*, " +
				"concat(t_activity.startDate, ' ', t_activity.name, ' ', t_activity.description) as activityDesc "
		}
		sql += " from t_photos " + joinSql + " where "
		sql += strings.Join(cnds, " and ")
		if !req.IdOnly {
			sql += fmt.Sprintf(" limit %d, %d", req.Start, req.Size)
		}
		log.Println("sql is ", sql)
		var results []map[string]interface{}

		tx := db().Raw(sql, params).Scan(&results)
		if tx.Error != nil {
			log.Println("getPics error", tx.Error)
		}
		log.Println("results is ", results)
		if !req.IdOnly {
			results = *addThumbField(&results)
		}
		c.JSON(200, Response{0, map[string]interface{}{
			"totalRows": total,
			"startRow":  req.Start,
			"endRow":    req.Start + len(results),
			"data":      results}})
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

func addThumbField(records *[]map[string]interface{}) *[]map[string]interface{} {
	for _, r := range *records {
		r["thumb"] = getThumbUrl(r["name"].(string), r["rotate"].(int64))
		t := r["takenDate"].(time.Time)
		r["theYear"] = t.Year()
		r["theMonth"] = t.Month()
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
