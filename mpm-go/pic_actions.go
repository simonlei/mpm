package main

import (
	"mpm-go/model"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

func trashPhotos(c *gin.Context) {
	var ids []int
	c.BindJSON(&ids)
	x := db().Exec(`update t_photos set trashed=!trashed where id in (?)`, ids)
	c.JSON(http.StatusOK, Response{0, x.RowsAffected})
}

type EmptyTrashTask struct {
	BaseProgress
}

func (e *EmptyTrashTask) Run() {
	var photos []model.TPhoto
	db().Model(&model.TPhoto{}).Where("trashed = true").Scan(&photos)
	e.Total = len(photos)
	e.Count = 0
	for _, p := range photos {
		realDelete(&p)
		time.Sleep(10 * time.Millisecond)
		e.Count++
	}
}

func emptyTrash(c *gin.Context) {
	t := EmptyTrashTask{
		BaseProgress: BaseProgress{
			Total: -1,
			Count: 0,
		},
	}
	taskId := addTask(&t)
	safeGo(t.Run)
	c.JSON(200, Response{0, map[string]string{"taskId": taskId}})
}
