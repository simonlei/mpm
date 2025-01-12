package main

import (
	"net/http"
	"slices"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func getAllTags(c *gin.Context) {
	c.JSON(http.StatusOK, Response{0, getTagsFromDb()})
}

func getTagsFromDb() []string {
	var result []string
	db().Table("photo_tags").Select("name").Scan(&result)
	return result
}

func setTagsRelation(id int, tags string) {
	tagList := strings.Split(tags, ",")
	if len(tagList) == 0 {
		db().Exec("delete from photo_tags where photoId=?", id)
		return
	}
	db().Transaction(func(tx *gorm.DB) error {
		tx.Exec("delete from photo_tags where photoId=? and name not in (?)", id, tagList)
		var existNames []string
		tx.Raw("select name from photo_tags where photoId=?", id).Scan(&existNames)
		for _, t := range tagList {
			if !slices.Contains(existNames, t) {
				tx.Exec("insert into photo_tags (name, photoId) values (?, ?)", t, id)
			}
		}
		return nil
	})
}
