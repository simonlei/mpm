package main

import (
	"fmt"
	"mpm-go/model"
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
	sql := `select f.id, f.path, concat(f.name,'(',count(distinct p.id),')') title, f.parentId parent_id
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
		parentIdCnd = " f.parentId=-1"
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

type SwitchTrashFolderSchema struct {
	To   bool   `json:"to"`
	Path string `json:"path"`
}

func switchTrashFolder(c *gin.Context) {
	var req SwitchTrashFolderSchema
	c.BindJSON(&req)
	affected := db().Exec(`
		update t_photos
		inner join t_files on t_photos.id=t_files.photoId
		SET trashed = ?
		where t_files.path like ? and trashed != ?
	`, req.To, req.Path+"%", req.To).RowsAffected
	c.JSON(200, Response{0, affected})
}

type UpdateFolderDateSchema struct {
	Path   string `json:"path"`
	ToDate string `json:"toDate"`
}

func updateFolderDate(c *gin.Context) {
	var req UpdateFolderDateSchema
	c.BindJSON(&req)
	affected := db().Exec(`
		update t_photos
	    inner join t_files on t_photos.id=t_files.photoId
	     SET taken_date = ?
	     where t_files.path like ?
	`, req.ToDate, req.Path+"%").RowsAffected
	c.JSON(200, Response{0, affected})
}

type UpdateFolderGisSchema struct {
	Path      string  `json:"path"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

func updateFolderGis(c *gin.Context) {
	var req UpdateFolderGisSchema
	c.BindJSON(&req)
	address := getGisAddress(req.Latitude, req.Longitude)
	affected := db().Exec(`
		update t_photos
		inner join t_files on t_photos.id=t_files.photoId
		SET latitude = ?, longitude=?, address=?
		where t_files.path like ?
	`, req.Latitude, req.Longitude, address, req.Path+"%").RowsAffected
	c.JSON(200, Response{0, affected})
}

type FolderActionSchema struct {
	FromPath string `json:"fromPath"`
	ToId     string `json:"toId"`
	Merge    bool   `json:"merge"`
}

func moveFolder(c *gin.Context) {
	var req FolderActionSchema
	c.BindJSON(&req)
	l.Infof("Move Folder ", req)
	var node model.TFile
	db().Where("path = ?", req.FromPath).Find(&node)
	var newParent model.TFile
	db().Where("id = ?", req.ToId).Find(&newParent)
	if req.Merge {
		mergeTo(&node, &newParent)
	} else {
		moveFolderTo(&node, &newParent)
	}
	c.JSON(200, Response{0, true})
}

func moveFolderTo(child *model.TFile, newParent *model.TFile) {
	if newParent.ID == 0 || child.ID == 0 {
		return
	}
	l.Infof("Child {} parent id {}", child.ID, newParent.ID)
	// 如果 newParent 下面有同名 node，那么将node merge到newParent下的同名node下
	var sameNode model.TFile
	db().Where("parentId =? and name =?", newParent.ID, child.Name).Find(&sameNode)
	if !child.IsFolder || sameNode.ID == 0 || sameNode.ID == child.ID {
		child.ParentID = newParent.ID
		newPath := newParent.Path + "/" + child.Name
		l.Infof("New path {} {}", newPath, child.ParentID)
		child.Path = newPath
		db().Save(child)
		var children []model.TFile
		db().Where("parentId =?", child.ID).Find(&children)
		for _, sub := range children {
			moveFolderTo(&sub, child)
		}
	} else {
		mergeTo(child, &sameNode)
	}
}

// 将node 下的所有目录和文件都转移到 newParent 目录下，并删除node
func mergeTo(node *model.TFile, newParent *model.TFile) {
	if newParent.ID == 0 {
		panic("不能合并到root下")
	}
	l.Infof("New parent id {}", newParent.ID)
	var children []model.TFile
	db().Where("parentId =?", node.ID).Find(&children)
	for _, sub := range children {
		moveFolderTo(&sub, newParent)
	}
	db().Delete(&node)
}
