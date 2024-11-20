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
	FromPhoto int64           `json:"fromPhoto"`
}

func createOrUpdateActivity(c *gin.Context) {
	var param ActivityParam
	c.BindJSON(&param)
	if param.Activity.ID == 0 {
		db().Create(&param.Activity)
		db().Model(&model.TPhoto{}).Where("id =?", param.FromPhoto).Update("activity", param.Activity.ID)
	} else {
		db().Model(&param.Activity).Updates(&param.Activity)
	}
}
