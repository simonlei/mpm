package main

import (
	"crypto/sha256"
	"fmt"
	"mpm-go/model"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

func checkPassword(c *gin.Context) {
	var req model.TUser
	c.BindJSON(&req)
	var user model.TUser
	db().First(&user, "account = ?", req.Account)
	if user.ID == 0 {
		panic("用户不存在")
	}
	calcedPwd := fmt.Sprintf("%X", sha256.Sum256([]byte(req.Passwd+user.Salt)))
	l.Info("Calced pwd is ", calcedPwd)
	if calcedPwd == user.Passwd {
		user.Salt = ""
		user.Passwd = ""
		user.Signature = calcSignature(req.Account, "")
	} else {
		panic("密码不正确")
	}
	c.JSON(200, Response{0, user})
}

func calcSignature(user, timestamp string) string {
	var token model.Meta
	db().First(&token, "`c_key` =?", "login_token")
	if token.ID == 0 {
		token.Key = "login_token"
		token.Value = uuid.NewString()
		db().Create(&token)
	}
	return getMD5Hash(user + token.Value + timestamp)
}

func createOrUpdateUser(c *gin.Context) {
	var req model.TUser
	c.BindJSON(&req)
	var user model.TUser
	if req.ID > 0 {
		db().First(&user, "id =?", req.ID)
	}
	if user.ID == 0 {
		req.ID = 0
		req.Salt = uuid.NewString()
		req.Passwd = fmt.Sprintf("%X", sha256.Sum256([]byte(req.Passwd+req.Salt)))
		db().Create(&req)
	} else {
		user.Name = req.Name
		user.FaceId = req.FaceId
		user.IsAdmin = req.IsAdmin
		if req.Passwd != "" {
			user.Passwd = fmt.Sprintf("%X", sha256.Sum256([]byte(req.Passwd+user.Salt)))
		}
		db().Save(&user)
	}
	c.JSON(200, Response{0, true})
}

func loadUsers(c *gin.Context) {
	var users []model.TUser
	db().Find(&users)
	c.JSON(200, Response{0, users})
}

func loadUser(c *gin.Context) {
	var req IdReq
	c.BindJSON(&req)
	var user model.TUser
	db().First(&user, "id =?", req.Id)
	user.Salt = ""
	user.Passwd = ""
	c.JSON(200, Response{0, user})
}

func deleteUser(c *gin.Context) {
	var req IdReq
	c.BindJSON(&req)
	db().Delete(&model.TUser{}, req.Id)
	c.JSON(200, Response{0, true})
}
