package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"mpm-go/model"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/magiconair/properties/assert"
	"gorm.io/gorm"
)

func TestCreateOrUpdateActivity(t *testing.T) {
	// 设置请求体
	c, w := createGinTest(`{"activity":{"start_date":"2023-10-16 16:54:32","end_date":"2023-10-16 16:54:32","latitude":null,"longitude":null,"fromPhoto":1253,"name":"aaaa","description":"bbbbbb"},"fromPhoto":1253}`)
	// 调用待测函数
	createOrUpdateActivity(c)
	// 检查响应状态码
	assert.Equal(t, w.Code, http.StatusOK)
	resp := w.Body.String()
	l.Info(resp)
	var a model.TActivity
	json.Unmarshal([]byte(resp), &a)
	assert.Equal(t, a.Name, "aaaa")

	// update
	a.Name = "aaaa2"
	bs, _ := json.Marshal(a)
	str := fmt.Sprintf(`{"activity":%s,"fromPhoto":1253}`, string(bs))

	c, w = createGinTest(str)
	createOrUpdateActivity(c)
	//assert.Equal(t, w.Code, http.StatusOK)
	json.Unmarshal(w.Body.Bytes(), &a)
	assert.Equal(t, a.Name, "aaaa2")

	c, w = createGinTest(string(bs))
	deleteActivity(c)
	//assert.Equal(t, http.StatusOK, w.Code)
	json.Unmarshal(w.Body.Bytes(), &a)
	assert.Equal(t, a.Name, "aaaa2")

	result := db().First(&a, a.ID)
	assert.Equal(t, errors.Is(result.Error, gorm.ErrRecordNotFound), true)
}

func createGinTest(req string) (*gin.Context, *httptest.ResponseRecorder) {
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	// 设置请求体
	c.Request, _ = http.NewRequest("POST", "/", bytes.NewBufferString(req))
	return c, w
}
