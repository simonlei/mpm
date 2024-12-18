package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

type FoldersDataRequest struct {
	Trashed  bool
	Star     bool
	ParentId int
}

type FoldersResp struct {
	Id       int    `json:"id"`
	Path     string `json:"path"`
	Title    string `json:"title"`
	ParentId int    `json:"parentid"`
}

func getFoldersData(c *gin.Context) {
	var req FoldersDataRequest
	err := c.BindJSON(&req)
	if err != nil {
		l.Info("Can't bind request:", err)
		return
	}
	sql := `select f.id, f.path, concat(f.name,'(',count(distinct p.id),')') title, if (f.parentId is null, -1, f.parentId) parent_id
	               from t_files f
	               left join t_files fp on fp.path like concat(f.path, '%%')
	               left join t_photos p  on fp.photoId=p.id
	               where f.isFolder=true and %s
	               and p.trashed=? %s
	               group by f.id
	               order by `
	var parentIdCnd string
	if req.ParentId <= 0 {
		sql += "f.id"
		parentIdCnd = " f.parentId is null"
	} else {
		sql += "f.name"
		parentIdCnd = fmt.Sprintf(" f.parentId=%d", req.ParentId)
	}
	starCnd := ""
	if req.Star {
		starCnd = " and p.star=true"
	}

	var resp []FoldersResp
	sql = fmt.Sprintf(sql, parentIdCnd, starCnd)
	l.Info("sql is ", sql)
	db().Raw(sql, req.Trashed).Find(&resp)
	c.JSON(http.StatusOK, Response{0, resp})
}
