package main

import (
	"mpm-go/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

func getActivitiesApi(c *gin.Context) {
	var as []model.TActivity

	db().Find(&as)
	c.JSON(http.StatusOK, Response{0, as})
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
