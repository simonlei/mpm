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
	Star     bool   `json:"star"`
	Video    bool   `json:"video"`
	Trashed  bool   `json:"trashed"`
	IdOnly   bool   `json:"id_only"`
	Start    int    `json:"start"`
	FaceId   int    `json:"face_id"`
	IdRank   int    `json:"id_rank"`
	Size     int    `json:"size" default:"75"`
	DateKey  string `json:"date_key"`
	Path     string `json:"path"`
	Tag      string `json:"tag"`
	Order    string `json:"order" default:"id"`
}

func getPics(c *gin.Context) {
	var req GetPicsRequest
	err := c.BindJSON(&req)
	if err != nil {
		l.Info("Can't bind request:", err)
		return
	}
	// 如果 order 为空，设置默认值为 -id
	if req.Order == "" {
		req.Order = "-id"
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
		joinSql += "inner join t_files on t_photos.id = t_files.photo_id "
		cnds = append(cnds, "t_files.path like @filePath")
		params["filePath"] = req.Path + "%"
	}
	if req.FaceId > 0 {
		joinSql += "inner join photo_face_info f on t_photos.id=f.photo_id "
		cnds = append(cnds, "f.face_id = @face_id")
		params["face_id"] = req.FaceId
	}
	cnds = append(cnds, "trashed = "+strconv.FormatBool(req.Trashed))
	if req.Star {
		cnds = append(cnds, "star = "+strconv.FormatBool(req.Star))
	}
	if req.Video {
		cnds = append(cnds, "media_type = 'video' ")
	}
	if req.Tag != "" {
		cnds = append(cnds, "concat(',', tags, ',') like '%,"+req.Tag+",%'")
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
				"concat(t_activity.start_date, ' ', t_activity.name, ' ', t_activity.description) as activity_desc "
		}
		sql += " from t_photos " + joinSql + " where "
		sql += strings.Join(cnds, " and ")
		sql += " order by t_photos." + req.Order
		if desc {
			sql += " desc"
		}
		l.Info("sql is ", sql)
		if req.IdOnly {
			var results []map[string]any

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

type UpdateImageRequest struct {
	ID          int              `json:"id"`
	Star        *bool            `json:"star"`
	Trashed     *bool            `json:"trashed"`
	Activity    *int             `json:"activity"`
	Longitude   *float64         `json:"longitude"`
	Latitude    *float64         `json:"latitude"`
	Tags        *string          `json:"tags"`
	TakenDate   *model.DateTime  `json:"taken_date"`
	Description *string          `json:"description"`
	Address     *string          `json:"address"`
	Rotate      *int             `json:"rotate"`
}

func updateImage(c *gin.Context) {
	var req UpdateImageRequest
	if err := c.BindJSON(&req); err != nil {
		l.Info("Can't bind request:", err)
		c.JSON(400, Response{1, "Invalid request"})
		return
	}

	// 处理活动相关更新
	updatePhotoActivity(&req)

	// 处理地理位置
	if (req.Longitude != nil && *req.Longitude > 0) || (req.Latitude != nil && *req.Latitude > 0) {
		lo := 0.0
		la := 0.0
		if req.Longitude != nil {
			lo = *req.Longitude
		}
		if req.Latitude != nil {
			la = *req.Latitude
		}
		address := getGisAddress(la, lo)
		if address != "" {
			req.Address = &address
		}
	}

	// 处理标签
	if req.Tags != nil {
		setTagsRelation(req.ID, *req.Tags)
	}

	// 直接使用 struct 更新
	db().Model(&model.TPhoto{}).Where("id=?", req.ID).Updates(req)

	var p model.TPhoto
	db().First(&p, req.ID)
	c.JSON(200, Response{0, addThumbField([]*model.TPhoto{&p})[0]})
}

// 通过 ID 获取单张照片信息
func getPhotoById(c *gin.Context) {
	var req IdReq
	err := c.BindJSON(&req)
	if err != nil {
		l.Info("Can't bind request:", err)
		c.JSON(400, Response{1, "Invalid request"})
		return
	}

	var photo model.TPhoto
	result := db().Raw(`
		select t_photos.*, 
		concat(t_activity.start_date, ' ', t_activity.name, ' ', t_activity.description) as activity_desc
		from t_photos
		left join t_activity on t_activity.id=t_photos.activity
		where t_photos.id=?
	`, req.Id).Scan(&photo)

	if result.Error != nil {
		l.Info("getPhotoById error:", result.Error)
		c.JSON(500, Response{1, "Error fetching photo"})
		return
	}

	if photo.ID == 0 {
		c.JSON(404, Response{1, "Photo not found"})
		return
	}

	photos := addThumbField([]*model.TPhoto{&photo})
	c.JSON(200, Response{0, photos[0]})
}

// 更改活动 id

func updatePhotoActivity(req *UpdateImageRequest) {
	if req.Activity == nil {
		return
	}
	var ac model.TActivity
	db().First(&ac, *req.Activity)
	var photo model.TPhoto
	db().First(&photo, req.ID)
	if ac.ID == 0 || photo.ID == 0 {
		return
	}
	takeDate := time.Time(photo.TakenDate)
	startDate := time.Time(ac.StartDate)
	endDate := time.Time(ac.EndDate)
	if takeDate.Before(startDate) || takeDate.After(endDate.Add(24*time.Hour)) {
		// 如果当前时间不在活动范围内，更改时间为活动开始时间
		startDateTime := model.DateTime(startDate)
		req.TakenDate = &startDateTime
	}
	if photo.Latitude == 0 && photo.Longitude == 0 {
		// 如果当前 GIS 是空，更改 GIS 为活动的 GIS
		req.Longitude = &ac.Longitude
		req.Latitude = &ac.Latitude
	}
}

// fixZeroDimensionPhotos 修复宽高为0的图片和视频
func fixZeroDimensionPhotos(c *gin.Context) {
	l.Info("Starting to fix photos/videos with zero dimensions")
	
	total, success, err := fixPhotosWithZeroDimensions()
	if err != nil {
		l.Error("Failed to fix photos/videos:", err)
		c.JSON(500, Response{1, fmt.Sprintf("Failed to fix photos/videos: %v", err)})
		return
	}

	c.JSON(200, Response{0, map[string]interface{}{
		"total":   total,
		"success": success,
		"failed":  total - success,
		"message": fmt.Sprintf("Fixed %d out of %d photos/videos", success, total),
	}})
}

// forceFixPhotoByIdApi 强制修复指定ID的照片/视频
func forceFixPhotoByIdApi(c *gin.Context) {
	var req IdReq
	if err := c.BindJSON(&req); err != nil {
		l.Error("Can't bind request:", err)
		c.JSON(400, Response{1, "Invalid request"})
		return
	}

	l.Info("Force fixing photo/video with ID:", req.Id)

	if err := forceFixPhotoById(int64(req.Id)); err != nil {
		l.Error("Failed to force fix photo/video:", err)
		c.JSON(500, Response{1, fmt.Sprintf("Failed to force fix: %v", err)})
		return
	}

	// 修复成功后，重新查询照片信息返回
	var photo model.TPhoto
	if err := db().First(&photo, req.Id).Error; err != nil {
		l.Error("Failed to fetch updated photo:", err)
		c.JSON(500, Response{1, "Fixed but failed to fetch updated data"})
		return
	}

	photos := addThumbField([]*model.TPhoto{&photo})
	c.JSON(200, Response{0, map[string]interface{}{
		"message": "Photo/video fixed successfully",
		"photo":   photos[0],
	}})
}

