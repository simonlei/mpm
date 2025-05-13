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
	}
	c.JSON(200, user)
}

func calcSignature(user, timestamp string) string {
	var token model.Meta
	db().First(&token, "key =?", "login_token")
	if token.ID == 0 {
		token.Key = "login_token"
		token.Value = uuid.NewString()
		db().Create(&token)
	}
	return getMD5Hash(user + token.Value + timestamp)
}

/*
- [ ] CheckPassword: '/checkPassword',
- [ ] CreateOrUpdateUser: '/createOrUpdateUser',
- [ ] LoadUsers: '/loadUsers',
- [ ] LoadUser: '/loadUser',
- [ ] DeleteUser: '/deleteUser',

*/
