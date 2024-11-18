package main

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
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
