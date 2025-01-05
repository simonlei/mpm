package main

import (
	"bytes"
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"github.com/gin-contrib/static"
	"github.com/gin-gonic/gin"
	cachecontrol "go.eigsys.de/gin-cachecontrol/v2"
)

func main() {
	setupEngine()
}

func setupEngine() {
	r := gin.New()
	r.Use(MpmMiddleWare(), MpmRecovery())

	base := getEnvIgnoreCase("static_base") // ../mpm-vue3/dist/
	l.Info(base)
	r.Use(static.Serve("/", static.LocalFile(base, true)))
	r.POST("/api/getActivities", getActivitiesApi)
	r.POST("/api/getPicsDate", getPicsDate)
	r.POST("/api/getPics", getPics)
	r.POST("/api/getCount", getCount)
	r.POST("/api/getAllTags", getAllTags)
	r.POST("/api/getFoldersData", getFoldersData)
	r.POST("/api/getFaces", getFaces)
	r.POST("/api/getFacesWithName", getFacesWithName)
	r.POST("/api/getFacesForPhoto", getFacesForPhoto)
	r.GET("/geo_json_api/loadMarkersGeoJson", loadMarkersGeoJson)
	r.GET("/get_face_img/:faceId/:infoId", getFaceImg)

	r.POST("/api/createOrUpdateActivity", createOrUpdateActivity)
	r.POST("/api/deleteActivity", deleteActivity)
	r.POST("/api/trashPhotos", trashPhotos)
	r.POST("/api/uploadPhoto", uploadPhoto)

	r.GET("/cos/*path", cachecontrol.New(cachecontrol.CacheAssetsForeverPreset), proxyCos)

	configForward(r)

	r.Run(":18880")
}

func configForward(r *gin.Engine) {
	r.NoRoute(proxyJava)
}

func proxyJava(c *gin.Context) {
	remote, _ := url.Parse("http://127.0.0.1:" + getEnvIgnoreCaseWithDefault("SERVER_PORT", "8080"))
	l.Info(remote)
	director := func(req *http.Request) {
		req.URL.Scheme = remote.Scheme
		req.URL.Host = remote.Host
		req.URL.Path = remote.Path + c.Request.URL.Path
		req.Header = c.Request.Header
	}
	proxy := &httputil.ReverseProxy{Director: director}
	proxy.ServeHTTP(c.Writer, c.Request)
	l.Info("Forwarded request ", c.Request.URL)
}

func MpmMiddleWare() gin.HandlerFunc {
	return func(c *gin.Context) {
		// hasRight := AuthFilter(c)
		hasRight := true
		bodyWriter := &BodyWriter{buf: &bytes.Buffer{}, ResponseWriter: c.Writer}

		if !hasRight {
			c.String(http.StatusForbidden, "无权限访问，请确认已正确登录或是正确的 API 调用方式")
			c.Abort()
		} else {
			if !c.IsAborted() {
				c.Writer = bodyWriter
				c.Next()
				contentType := c.Writer.Header().Get("Content-Type")

				l.Info(c.Request.Context(), "Content type is :", contentType)
				if strings.HasPrefix(contentType, "application/json") {
					// 拿到 response 之后再包一层
					bodyWriter.wrapJsonRpc(c)
				} else {
					bodyWriter.directWrite()
				}
			}
		}

	}
}

func MpmRecovery() func(c *gin.Context) {
	return gin.CustomRecovery(func(c *gin.Context, err interface{}) {
		msg := fmt.Sprintf("请求-%s,参数-%s, 返回-%d:%s,访问-%s", c.Request.URL, c.Request.Body, c.Writer.Status(), err, c.Request.Referer())
		l.Error(msg)

		c.Writer.Header().Set("content-type", "application/json; charset=utf-8")
		c.String(http.StatusOK, fmt.Sprintf(`{
			"jsonrpc": "2.0",
			"error" : {
				"message": "%s",
				"code": -20001
			}
		}`, err))
		i := c.Writer

		writer, ok := i.(*BodyWriter)
		if ok {
			writer.directWrite()
		}
	})
}

type BodyWriter struct {
	gin.ResponseWriter
	buf *bytes.Buffer
}

func (cw BodyWriter) Write(b []byte) (int, error) {
	return cw.buf.Write(b)
}

func (cw BodyWriter) wrapJsonRpc(_ *gin.Context) {
	// TODO: 处理成 response
	// cw.ResponseWriter.Write([]byte(`{"id": "techmap-vue3","jsonrpc": "2.0"`))
	cw.ResponseWriter.Write(cw.buf.Bytes())
	// cw.ResponseWriter.Write([]byte("}"))
}

func (cw BodyWriter) directWrite() {
	cw.ResponseWriter.Write(cw.buf.Bytes())
}
