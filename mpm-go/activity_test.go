package main

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestCreateOrUpdateActivity(t *testing.T) {
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	// 设置请求体
	c.Request, _ = http.NewRequest("POST", "/", bytes.NewBufferString(`{"activity":{"startDate":"2023-10-16 16:54:32","endDate":"2023-10-16 16:54:32","latitude":null,"longitude":null,"fromPhoto":1253,"name":"aaaa","description":"bbbbbb"},"fromPhoto":1253}`))

	// 调用待测函数
	createOrUpdateActivity(c)

	// 检查响应状态码
	if w.Code != http.StatusOK {
		t.Errorf("Expected status code %d, got %d", http.StatusOK, w.Code)
	}

	// 可以根据需要添加更多的断言来检查数据库操作的结果
}
