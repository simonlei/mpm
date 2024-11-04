package main

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/yudai/gojsondiff"
	"github.com/yudai/gojsondiff/formatter"
)

type TestResponseRecorder struct {
	*httptest.ResponseRecorder
	closeChannel chan bool
}

func (r *TestResponseRecorder) CloseNotify() <-chan bool {
	return r.closeChannel
}

func (r *TestResponseRecorder) closeClient() {
	r.closeChannel <- true
}

func CreateTestResponseRecorder() *TestResponseRecorder {
	return &TestResponseRecorder{
		httptest.NewRecorder(),
		make(chan bool, 1),
	}
}

type TestFun struct {
	name   string
	f      func(c *gin.Context)
	url    string
	params string
}

func Test_JavaGoDiff(t *testing.T) {
	tests := []TestFun{
		{
			name:   "getActivities",
			f:      getActivitiesApi,
			url:    "/api/getActivities",
			params: "{}",
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			javaResult := javaResult(tt)
			goResult := goResult(tt)

			l.Info("Java:", javaResult)
			l.Info("Go:", goResult)

			// 创建差异比较器
			differ := gojsondiff.New()
			// 计算两个 JSON 的差异
			diff, err := differ.Compare([]byte(goResult), []byte(javaResult))
			if err != nil {
				panic(err)
			}
			if diff.Modified() {
				// 创建格式化器以输出差异
				x := formatter.NewDeltaFormatter()
				result, err := x.Format(diff)
				if err != nil {
					panic(err)
				}
				// panic(result)
				l.Info(result)
			} else {
				l.Info("No differences found.")
			}
		})
	}
}

func goResult(tt TestFun) string {
	recorder := CreateTestResponseRecorder()
	c, _ := gin.CreateTestContext(recorder)
	c.Request, _ = http.NewRequest("POST", tt.url, bytes.NewBufferString(tt.params))
	tt.f(c)
	return recorder.Body.String()
}

func javaResult(tt TestFun) string {
	recorder := CreateTestResponseRecorder()
	c, _ := gin.CreateTestContext(recorder)
	c.Request, _ = http.NewRequest("POST", tt.url, bytes.NewBufferString(tt.params))
	proxyJava(c)
	return recorder.Body.String()
}
