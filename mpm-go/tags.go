package main

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

func getAllTags(c *gin.Context) {
	c.JSON(http.StatusOK, Response{0, getTagsFromDb()})
}

func getTagsFromDb() []string {
	var result []string
	db().Table("photo_tags").Select("name").Scan(&result)
	return result
}
