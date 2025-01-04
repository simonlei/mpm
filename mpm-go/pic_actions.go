package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func trashPhotos(c *gin.Context) {
	var names []string
	c.BindJSON(&names)
	x := db().Exec(`update t_photos set trashed=!trashed where name in (?)`, names)
	c.JSON(http.StatusOK, x.RowsAffected)
}
