package main

import (
	"context"
	"encoding/base64"
	"fmt"
	"io"
	"math"
	"mpm-go/model"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/tencentcloud/tencentcloud-sdk-go/tencentcloud/common"
	v20200303 "github.com/tencentcloud/tencentcloud-sdk-go/tencentcloud/iai/v20200303"
	"github.com/tencentyun/cos-go-sdk-v5"
	"gorm.io/gorm"
)

type FaceGetRequest struct {
	ShowHidden bool `json:"showHidden"`
	Page       int
	Size       int
	NameFilter string `json:"nameFilter"`
}

type FaceGetResp struct {
	PersonId     string `gorm:"column:personId" json:"personId"`
	FaceId       int    `gorm:"column:faceId" json:"faceId"`
	Name         string `json:"name"`
	SelectedFace int    `gorm:"column:selectedFace" json:"selectedFace"`
	Collected    int    `json:"collected"`
	Hidden       int    `json:"hidden"`
	Count        int    `json:"count"`
}

type FaceNameResp struct {
	FaceId int    `gorm:"column:faceId" json:"faceId"`
	Name   string `gorm:"column:name" json:"name"`
}

func getFaces(c *gin.Context) {
	var req FaceGetRequest
	c.BindJSON(&req)
	sqlStr := `select personId, i.faceId, name, selectedFace, collected, hidden, count(*) as count
	    	from photo_face_info i
	    	left join t_face on t_face.id=i.faceId
	    	where personId is not null`
	sqlStr = setConditions(req.ShowHidden, req.NameFilter, sqlStr)

	countSql := "select count(*) c from (" + sqlStr + ") t"
	var count int
	db().Raw(countSql).Scan(&count)

	limit := fmt.Sprintf(" limit %d, %d", (req.Page-1)*req.Size, req.Size)
	var data []FaceGetResp
	db().Raw(sqlStr + limit).Find(&data)

	c.JSON(http.StatusOK, Response{0, map[string]interface{}{
		"total": count,
		"faces": data,
	}})
}

func setConditions(showHidden bool, nameFilter, s string) string {
	if !showHidden {
		s = s + " and t_face.hidden=false"
	}
	if strings.TrimSpace(nameFilter) != "" {
		s = s + " and name like '%" + strings.TrimSpace(nameFilter) + "%'"
	}
	return s + " group by i.faceId order by collected desc, count(*) desc, i.faceId"
}

func getFacesWithName(c *gin.Context) {
	s := `select t_face.id as faceId, name
        	from t_face
        	left join photo_face_info p on p.faceId=t_face.id
            where name is not null
            group by t_face.id
            order by count(*) desc`
	var data []FaceNameResp
	db().Raw(s).Find(&data)
	c.JSON(http.StatusOK, Response{0, data})
}

type IdReq struct {
	Id int
}

type FacesForPhotoResp struct {
	Id     int    `gorm:"column:id" json:"id"`
	FaceId int    `gorm:"column:faceId" json:"faceId"`
	X      int    `gorm:"column:x" json:"x"`
	Y      int    `gorm:"column:y" json:"y"`
	Width  int    `gorm:"column:width" json:"width"`
	Height int    `gorm:"column:height" json:"height"`
	Name   string `gorm:"column:name" json:"name"`
}

func getFacesForPhoto(c *gin.Context) {
	var req IdReq
	c.BindJSON(&req)
	s := `select pf.id, pf.faceId, x, y, width, height, name
	        from photo_face_info pf
            inner join t_face f on f.id=pf.faceId
            where pf.photoId=%d`
	var data []FacesForPhotoResp
	db().Raw(fmt.Sprintf(s, req.Id)).Find(&data)
	c.JSON(http.StatusOK, Response{0, data})
}

func getFaceImg(c *gin.Context) {
	faceId, _ := strconv.Atoi(c.Param("faceId"))
	infoId, _ := strconv.Atoi(c.Param("infoId"))
	var x *gorm.DB
	if infoId <= 0 {
		x = db().Raw("select * from photo_face_info where faceId=? order by height desc limit 1", faceId)
	} else {
		x = db().Raw("select * from photo_face_info where id=?", infoId)
	}
	var faceInfo model.PhotoFaceInfo
	x.Scan(&faceInfo)
	l.Info("face info is ", faceInfo.ID)
	var photo model.TPhoto
	db().First(&photo, faceInfo.PhotoId)
	getWiderFace(&photo, &faceInfo)
	cosObj := getFaceFromCos(&photo, &faceInfo)
	c.Header("Cache-Control", "max-age=31536000")
	c.Header("content-length", strconv.Itoa(int(cosObj.ContentLength)))
	c.DataFromReader(200, cosObj.ContentLength, cosObj.Response.Header.Get("content-type"), cosObj.Body, nil)
}

func getFaceFromCos(photo *model.TPhoto, faceInfo *model.PhotoFaceInfo) *cos.Response {
	req := "/small/" + photo.Name
	param := "imageMogr2/format/jpeg"
	if faceInfo != nil {
		getWiderFace(photo, faceInfo)
		param = "imageMogr2/cut/" + strconv.Itoa(faceInfo.Width) +
			"x" + strconv.Itoa(faceInfo.Height) +
			"x" + strconv.Itoa(faceInfo.X) +
			"x" + strconv.Itoa(faceInfo.Y) + "|" + param
	}
	obj, _ := Cos().CI.Get(context.Background(), req, param, nil)
	return obj
}

func getWiderFace(photo *model.TPhoto, faceInfo *model.PhotoFaceInfo) {
	padX := float64(faceInfo.Width) * 0.3
	padY := float64(faceInfo.Height) * 0.3
	x := float64(faceInfo.X) - padX
	y := float64(faceInfo.Y) - padY
	w := x + float64(faceInfo.Width) + 2*padX
	h := y + float64(faceInfo.Height) + 2*padY
	x2 := 0.0
	y2 := 0.0
	w2 := float64(photo.Width)
	h2 := float64(photo.Height)

	faceInfo.X = int(math.Max(x, x2))
	faceInfo.Y = int(math.Max(y, y2))
	faceInfo.Width = int(math.Min(w, w2) - float64(faceInfo.X))
	faceInfo.Height = int(math.Min(h, h2) - float64(faceInfo.Y))
}

type FaceUpdateParam struct {
	FaceId       int    `json:"faceId"`
	Name         string `json:"name"`
	SelectedFace int64  `json:"selectedFace"`
	Hidden       *bool  `json:"hidden"`
	Collected    *bool  `json:"collected"`
}

func updateFace(c *gin.Context) {
	var face FaceUpdateParam
	c.BindJSON(&face)
	var entityFace model.TFace
	db().First(&entityFace, face.FaceId)
	if entityFace.ID == 0 {
		c.JSON(http.StatusOK, Response{0, false})
		return
	}
	if face.Name != "" {
		var existFace model.TFace
		db().First(&existFace, "name=?", face.Name)
		if existFace.ID != 0 {
			c.JSON(http.StatusOK, Response{0, false})
			return
		}
		entityFace.Name = face.Name
	}
	if face.SelectedFace > 0 {
		// 设置对应的图片当中的人脸来当做默认人脸
		var faceInfo model.PhotoFaceInfo
		db().First(&faceInfo, "photoId=? and faceId=?", face.SelectedFace, face.FaceId)
		if faceInfo.ID > 0 {
			entityFace.SelectedFace = faceInfo.ID
		}
	}
	if face.Hidden != nil {
		entityFace.Hidden = *face.Hidden
	}
	if face.Collected != nil {
		entityFace.Collected = *face.Collected
	}
	db().Save(entityFace)
	c.JSON(http.StatusOK, Response{0, true})
}

type MergeFaceParam struct {
	From int `json:"from"`
	To   int `json:"to"`
}

func mergeFace(c *gin.Context) {
	var r MergeFaceParam
	c.BindJSON(&r)
	var face model.TFace
	db().First(&face, r.From)
	req := v20200303.NewDeletePersonFromGroupRequest()
	req.GroupId = getGroupName()
	req.PersonId = &face.PersonId
	// 这里也要把腾讯云上的 face 给删掉
	resp, err := Iai().DeletePersonFromGroup(req)

	l.Infof("Delete person from group response:{}, err:{}", resp, err)
	db().Transaction(func(tx *gorm.DB) error {
		tx.Exec("delete from t_face where id=?", r.From)
		tx.Exec("update photo_face_info set faceId=? where faceId=?", r.To, r.From)
		return nil
	})
	c.JSON(http.StatusOK, Response{0, true})
}

func getGroupName() *string {
	name := "faceGroup"
	if viper.GetBool("isDev") {
		name = "faceGroup-dev"
	}
	return &name
}

func removePhotoFaceInfo(c *gin.Context) {
	var req IdReq
	c.BindJSON(&req)
	db().Exec("delete from photo_face_info where id=?", req.Id)
	c.JSON(http.StatusOK, Response{0, 1})
}

func rescanFace(c *gin.Context) {
	var req IdReq
	c.BindJSON(&req)
	var photo model.TPhoto
	db().First(&photo, req.Id)
	if photo.ID > 0 {
		detectFaceIn(photo)
	}
	c.JSON(http.StatusOK, Response{0, true})
}

func detectFaceIn(photo model.TPhoto) {
	faceInfos, err := detectFacesInPhoto(photo)
	if err != nil {
		l.Info("detect face error:", err)
		return
	}
	db().Transaction(func(tx *gorm.DB) error {
		tx.Exec("delete from photo_face_info where photoId=?", photo.ID)
		for _, faceInfo := range faceInfos {
			if *faceInfo.Width <= 64 || *faceInfo.Height <= 64 || *faceInfo.X < 0 || *faceInfo.Y < 0 {
				continue
			}
			// face info 应该记录下来，到时候可以在页面上点选对应的人头
			info := model.PhotoFaceInfo{
				PhotoId: photo.ID,
				X:       int(*faceInfo.X),
				Y:       int(*faceInfo.Y),
				Width:   int(*faceInfo.Width),
				Height:  int(*faceInfo.Height),
			}
			tx.Create(&info)
			addFaceToGroup(tx, photo, info)
		}
		return nil
	})
}

func addFaceToGroup(tx *gorm.DB, photo model.TPhoto, info model.PhotoFaceInfo) {
	img := getImage(photo, &info)
	// 一张照片里面可能有多个 face，所以 person id 应该是photo.id+faceInfo.id
	personId := common.StringPtr(strconv.Itoa(int(photo.ID)) + "-" + strconv.Itoa(int(info.ID)))
	req := v20200303.NewCreatePersonRequest()
	req.GroupId = getGroupName()
	req.Image = &img
	req.PersonId = personId
	req.PersonName = personId
	req.UniquePersonControl = common.Uint64Ptr(uint64(1))
	req.NeedRotateDetection = common.Uint64Ptr(uint64(1))

	resp, err := Iai().CreatePerson(req)
	if err != nil {
		l.Errorf("create person error: %s", err)
		return
	}
	faceId := *resp.Response.FaceId
	similarPersonId := resp.Response.SimilarPersonId
	// 有可能 entity face 被删除了的情况，这时要创建新的 face
	var face model.TFace
	if similarPersonId != nil {
		l.Infof("face info %s's similar person id %s ", photo.Name, *similarPersonId)
		tx.Where("personId=?", similarPersonId).First(&face)
	}
	if face.ID == 0 {
		face = model.TFace{
			FaceId:   parseInt64(faceId),
			PersonId: *personId,
		}
		tx.Create(&face)
	}
	info.FaceId = face.ID
	tx.Save(&info)
}

func detectFacesInPhoto(photo model.TPhoto) ([]*v20200303.FaceInfo, error) {
	image := getImage(photo, nil)
	var maxFace uint64 = 10
	var needRotate uint64 = 1
	req := v20200303.NewDetectFaceRequest()
	req.Image = &image
	req.MaxFaceNum = &maxFace
	req.NeedRotateDetection = &needRotate
	resp, err := Iai().DetectFace(req)
	if err != nil {
		return nil, err
	}
	return resp.Response.FaceInfos, nil
}

// 如果face为空，则从cos上获取整个图片，否则 cut 一下
func getImage(photo model.TPhoto, face *model.PhotoFaceInfo) string {
	object := getFaceFromCos(&photo, face)
	defer object.Body.Close() // 确保在函数结束时关闭资源

	bytes, err := io.ReadAll(object.Body)
	if err != nil {
		return ""
	}
	return base64.StdEncoding.EncodeToString(bytes)
}

func detectFaces() {
	scanPhotoDoTask("lastCheckDetectedFaces", func(photo *model.TPhoto) {
		detectFaceIn(*photo)
	}, 100)
}

func scanPhotoDoTask(taskName string, f func(photo *model.TPhoto), pageSize int) {
	var meta model.Meta
	db().First(&meta, "c_key=?", taskName)
	var lastId int64
	if meta.ID > 0 {
		lastId = parseInt64(meta.Value)
	} else {
		meta.Key = taskName
	}
	var photos []model.TPhoto
	db().Where("id > ?", lastId).Order("id").Limit(pageSize).Find(&photos)
	if len(photos) == 0 {
		return
	}
	l.Infof("Start to do photos %d", len(photos))
	for _, photo := range photos {
		defer func() {
			if r := recover(); r != nil {
				// 在这里处理panic，例如记录日志或发送通知
				l.Infof("Recovered from panic: %v", r)
			}
		}()
		f(&photo)
		lastId = photo.ID
		meta.Value = strconv.FormatInt(lastId, 10)
		saveMeta(&meta)
	}
}

func saveMeta(meta *model.Meta) {
	if meta.ID == 0 {
		db().Create(meta)
	} else {
		db().Save(meta)
	}
}
