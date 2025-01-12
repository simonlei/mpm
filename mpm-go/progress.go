package main

import (
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type ProgressInterface interface {
	Progress() int
	IsFinished() bool
	ProgressDetail() map[string]any
	Run()
}

type BaseProgress struct {
	Total int
	Count int
}

func (e BaseProgress) Progress() int {
	switch e.Total {
	case 0:
		return 100
	case -1:
		return -1
	default:
		return e.Count * 100 / e.Total
	}

}

func (e BaseProgress) IsFinished() bool {
	return e.Count >= e.Total
}

func (e BaseProgress) ProgressDetail() map[string]any {
	if e.IsFinished() {
		return map[string]any{"total": e.Total, "count": e.Total, "progress": 100}
	}
	return map[string]any{"total": e.Total, "count": e.Count, "progress": e.Progress()}
}

var tasks map[string]ProgressInterface = make(map[string]ProgressInterface)

func addTask(progress ProgressInterface) string {
	taskId := uuid.New().String()
	tasks[taskId] = progress
	return taskId
}

func getProgress(c *gin.Context) {
	taskId := c.Param("taskId")
	l.Infof("taskId %s", taskId)
	if t, ok := tasks[taskId]; ok {
		if t.IsFinished() {
			delete(tasks, taskId)
		}
		c.JSON(200, Response{0, t.ProgressDetail()})
	} else {
		c.JSON(200, Response{0, map[string]any{"progress": 100}})
	}
}
