package main

import (
	"context"
	"fmt"
	"math"
	"mpm-go/model"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
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
	return s + " group by faceId order by collected desc, count(*) desc, i.faceId"
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

type PhotoFaceInfo struct {
	gorm.Model
	PhotoId int64 `gorm:"column:photoId" json:"photoId"`
	FaceId  int64 `gorm:"column:faceId" json:"faceId"`
	X       int   `gorm:"column:x" json:"x"`
	Y       int   `gorm:"column:y" json:"y"`
	Width   int   `gorm:"column:width" json:"width"`
	Height  int   `gorm:"column:height" json:"height"`
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
	var faceInfo PhotoFaceInfo
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

func getFaceFromCos(photo *model.TPhoto, faceInfo *PhotoFaceInfo) *cos.Response {
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

func getWiderFace(photo *model.TPhoto, faceInfo *PhotoFaceInfo) {
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
