package main

import (
	"mpm-go/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

type ActivityWithCount struct {
	model.TActivity
	PhotoCount int64 `json:"photo_count"`
}

func getActivitiesApi(c *gin.Context) {
	var result []ActivityWithCount

	// 使用 LEFT JOIN 和 GROUP BY 一次性查询所有活动及其照片数量
	db().Table("t_activity").
		Select("t_activity.*, COALESCE(COUNT(t_photos.id), 0) as photo_count").
		Joins("LEFT JOIN t_photos ON t_activity.id = t_photos.activity").
		Group("t_activity.id").
		Scan(&result)

	c.JSON(http.StatusOK, Response{0, result})
}

type ActivityParam struct {
	Activity  model.TActivity `json:"activity"`
	FromPhoto *int64          `json:"fromPhoto"`
}

func createOrUpdateActivity(c *gin.Context) {
	var param ActivityParam
	err := c.BindJSON(&param)
	if err != nil {
		l.Info("Can't bind request:", err)
		return
	}
	if param.Activity.ID == 0 {
		db().Create(&param.Activity)
		if param.FromPhoto != nil {
			db().Model(&model.TPhoto{}).Where("id =?", param.FromPhoto).Update("activity", param.Activity.ID)
		}
	} else {
		db().Model(&param.Activity).Updates(&param.Activity)
	}
	c.JSON(http.StatusOK, Response{0, param.Activity})
}

func deleteActivity(c *gin.Context) {
	var a model.TActivity
	c.BindJSON(&a)
	db().Delete(&a)
	c.JSON(http.StatusOK, Response{0, a})
}
