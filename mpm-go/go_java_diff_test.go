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
			name:   "getCount",
			f:      getCount,
			url:    "/api/getCount",
			params: `{"trashed":true}`,
		},
		{
			name:   "getAllTags",
			f:      getAllTags,
			url:    "/api/getAllTags",
			params: `{}`,
		},

		{
			name:   "getActivities",
			f:      getActivitiesApi,
			url:    "/api/getActivities",
			params: "{}",
		},
		{
			name:   "getPicsDate",
			f:      getPicsDate,
			url:    "/api/getPicsDate",
			params: `{"trashed":false,"star":null}`,
		},
		{
			name:   "getPics ids",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"idOnly":true,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":"undefined","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPics",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"start":0,"size":10,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":null,"path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPicsYear",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"start":4,"size":10,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":"2022","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPicsYearMonth",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"idOnly":true,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":"202212","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPicsActivity",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"start":0,"size":150,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":"1000005","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPicsOrder",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"start":0,"size":150,"trashed":false,"star":null,"video":null,"order":"-size","dateKey":"202207","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getPicsOrderStared",
			f:      getPics,
			url:    "/api/getPics",
			params: `{"start":0,"size":150,"trashed":false,"star":true,"video":null,"order":"width","dateKey":"202207","path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getFoldersData",
			f:      getFoldersData,
			url:    "/api/getFoldersData",
			params: `{"parentId":null,"trashed":false,"star":null,"video":null,"order":"-taken_date","dateKey":null,"path":null,"tag":null,"faceId":null}`,
		},
		{
			name:   "getFaces",
			f:      getFaces,
			url:    "/api/getFaces",
			params: `{"showHidden":false,"page":1,"size":10,"nameFilter":""}`,
		},
		{
			name:   "getFacesFilterName",
			f:      getFaces,
			url:    "/api/getFaces",
			params: `{"showHidden":false,"page":1,"size":100,"nameFilter":"s"}`,
		},
		{
			name:   "getFacesWithName",
			f:      getFacesWithName,
			url:    "/api/getFacesWithName",
			params: `{}`,
		},
		{
			name:   "getFacesForPhoto",
			f:      getFacesForPhoto,
			url:    "/api/getFacesForPhoto",
			params: `{"id":17}`,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			javaResult := javaResult(tt)
			goResult := goResult(tt)

			// 创建差异比较器
			differ := gojsondiff.New()
			// 计算两个 JSON 的差异
			diff, err := differ.Compare([]byte(goResult), []byte(javaResult))
			if err != nil {
				panic(err)
			}
			if diff.Modified() {
				count := 0
				countDiff(diff.Deltas(), &count)
				l.Info("Count is ", count)
				if count > 0 {
					l.Info("Java:", javaResult)
					l.Info("Go:", goResult)
					// 创建格式化器以输出差异
					x := formatter.NewDeltaFormatter()
					result, err := x.Format(diff)
					if err != nil {
						panic(err)
					}
					panic(result)
					// l.Info(result)
				}
			} else {
				l.Info("No differences found.")
			}
		})
	}
}

func countDiff(deltas []gojsondiff.Delta, count *int) (deltaJson map[string]interface{}, err error) {
	deltaJson = map[string]interface{}{}
	for _, delta := range deltas {
		switch delta.(type) {
		case *gojsondiff.Object:
			d := delta.(*gojsondiff.Object)
			deltaJson[d.Position.String()], err = countDiff(d.Deltas, count)
			if err != nil {
				return nil, err
			}
		case *gojsondiff.Array:
			d := delta.(*gojsondiff.Array)
			deltaJson[d.Position.String()], err = countArrayDiff(d.Deltas, count)
			if err != nil {
				return nil, err
			}
		case *gojsondiff.Added:
			d := delta.(*gojsondiff.Added)
			l.Info("New value is ", d)
			if d.Value != nil {
				*count += 1
			}
		case *gojsondiff.Modified:
			d := delta.(*gojsondiff.Modified)
			if d.OldValue != nil && d.NewValue != nil {
				*count += 1
			}
		case *gojsondiff.TextDiff:
			*count += 1
		case *gojsondiff.Deleted:
			*count += 1
		case *gojsondiff.Moved:
			*count += 1
		default:
			*count += 1
		}
	}
	return
}

func countArrayDiff(deltas []gojsondiff.Delta, count *int) (deltaJson map[string]interface{}, err error) {
	deltaJson = map[string]interface{}{
		"_t": "a",
	}
	for _, delta := range deltas {
		switch delta.(type) {
		case *gojsondiff.Object:
			d := delta.(*gojsondiff.Object)
			deltaJson[d.Position.String()], err = countDiff(d.Deltas, count)
			if err != nil {
				return nil, err
			}
		case *gojsondiff.Array:
			d := delta.(*gojsondiff.Array)
			deltaJson[d.Position.String()], err = countArrayDiff(d.Deltas, count)
			if err != nil {
				return nil, err
			}
		case *gojsondiff.Added:
			*count += 1
		case *gojsondiff.Modified:
			d := delta.(*gojsondiff.Modified)
			if d.OldValue != nil && d.NewValue != nil {
				*count += 1
			}
		case *gojsondiff.TextDiff:
			*count += 1
		case *gojsondiff.Deleted:
			*count += 1
		case *gojsondiff.Moved:
			*count += 1
		default:
			*count += 1
		}
	}
	return
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
	c.Request.Header.Add("content-type", "application/json")
	proxyJava(c)
	return recorder.Body.String()
}
