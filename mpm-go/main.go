package main

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/gin-contrib/static"
	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	cachecontrol "go.eigsys.de/gin-cachecontrol/v2"
)

func main() {
	setupEngine()
}

func setupEngine() {
	safeGo(func() {
		for {
			detectFaces()
			time.Sleep(5 * time.Second)
		}
	})
	r := gin.New()
	r.Use(MpmMiddleWare(), MpmRecovery())

	base := viper.GetString("static.base") // ../mpm-vue3/dist/
	if base == "" {
		base = "/web"
	}
	l.Info(base)
	r.Use(static.Serve("/", static.LocalFile(base, true)))
	r.POST("/api/getActivities", getActivitiesApi)
	r.POST("/api/getPicsDate", getPicsDate)
	r.POST("/api/getPics", getPics)
	r.POST("/api/getPhotoById", getPhotoById)
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
	r.POST("/api/emptyTrash", emptyTrash)
	r.GET("/api/getProgress/:taskId", getProgress)
	r.POST("/api/switchTrashFolder", switchTrashFolder)
	r.POST("/api/updateFolderDate", updateFolderDate)
	r.POST("/api/updateFolderGis", updateFolderGis)
	r.POST("/api/moveFolder", moveFolder)
	r.POST("/api/updateImage", updateImage)
	r.POST("/api/updateFace", updateFace)
	r.POST("/api/mergeFace", mergeFace)
	r.POST("/api/removePhotoFaceInfo", removePhotoFaceInfo)
	r.POST("/api/rescanFace", rescanFace)
	r.POST("/api/checkPassword", checkPassword)
	r.POST("/api/createOrUpdateUser", createOrUpdateUser)
	r.POST("/api/loadUsers", loadUsers)
	r.POST("/api/loadUser", loadUser)
	r.POST("/api/deleteUser", deleteUser)
	r.POST("/api/totp", totp)
	r.GET("/api/fixZeroDimensionPhotos", fixZeroDimensionPhotos)
	r.POST("/api/forceFixPhotoById", forceFixPhotoByIdApi)

	r.GET("/cos/*path", cachecontrol.New(cachecontrol.CacheAssetsForeverPreset), proxyCos)

	r.Run(":" + viper.GetString("server.port"))
}

func MpmMiddleWare() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 记录请求开始时间
		startTime := time.Now()
		
		// 读取请求体（需要保存原始请求体供后续使用）
		var requestBody []byte
		if c.Request.Body != nil {
			requestBody, _ = io.ReadAll(c.Request.Body)
			// 重新设置请求体，因为ReadAll会消耗掉原始的Body
			c.Request.Body = io.NopCloser(bytes.NewBuffer(requestBody))
		}
		
		// 记录请求详情
		l.Infof("=== 请求开始 ===")
		l.Infof("请求方法: %s", c.Request.Method)
		l.Infof("请求URL: %s", c.Request.URL.String())
		l.Infof("请求IP: %s", c.ClientIP())
		l.Infof("User-Agent: %s", c.GetHeader("User-Agent"))
		l.Infof("Content-Type: %s", c.GetHeader("Content-Type"))
		
		// 记录重要的请求头
		if signature := c.GetHeader("Signature"); signature != "" {
			l.Infof("请求签名: %s", signature)
		}
		if account := c.GetHeader("Account"); account != "" {
			l.Infof("请求账号: %s", account)
		}
		
		// 记录请求体（只记录前500字符，避免日志过长）
		if len(requestBody) > 0 {
			bodyStr := string(requestBody)
			if len(bodyStr) > 500 {
				bodyStr = bodyStr[:500] + "...(truncated)"
			}
			l.Infof("请求体: %s", bodyStr)
		}
		
		// hasRight := AuthFilter(c)
		hasRight := true
		url := c.Request.URL.String()
		if url != "/api/checkPassword" && strings.HasPrefix(url, "/api") && c.GetHeader("Signature") != calcSignature(c.GetHeader("Account"), "") {
			// todo: 检查 timestamp，定个有效期
			hasRight = false
		}
		bodyWriter := &BodyWriter{buf: &bytes.Buffer{}, ResponseWriter: c.Writer}

		if !hasRight {
			l.Warnf("权限验证失败 - URL: %s, Account: %s", url, c.GetHeader("Account"))
			c.String(http.StatusForbidden, "签名不对，不允许访问 API")
			c.Abort()
		} else {
			if !c.IsAborted() {
				c.Writer = bodyWriter
				c.Next()
				contentType := c.Writer.Header().Get("Content-Type")

				l.Info("Content type is :", contentType)
				if strings.HasPrefix(contentType, "application/json") {
					// 拿到 response 之后再包一层
					bodyWriter.wrapJsonRpc(c)
				} else {
					bodyWriter.directWrite()
				}
			}
		}
		
		// 记录响应详情
		endTime := time.Now()
		duration := endTime.Sub(startTime)
		
		l.Infof("=== 请求结束 ===")
		l.Infof("响应状态码: %d", c.Writer.Status())
		l.Infof("响应大小: %d bytes", c.Writer.Size())
		l.Infof("处理耗时: %v", duration)
		
		// 记录响应体（只记录前500字符，避免日志过长）
		// 对于二进制内容（图片、视频等）不记录响应体
		if bodyWriter.buf != nil && bodyWriter.buf.Len() > 0 {
			contentType := c.Writer.Header().Get("Content-Type")
			isBinaryContent := isBinaryContentType(contentType)
			
			if isBinaryContent {
				l.Infof("响应体: [二进制内容，已跳过记录] Content-Type: %s", contentType)
			} else {
				responseBody := bodyWriter.buf.String()
				if len(responseBody) > 500 {
					responseBody = responseBody[:500] + "...(truncated)"
				}
				l.Infof("响应体: %s", responseBody)
			}
		}
		
		// 记录响应头中的重要信息
		if contentType := c.Writer.Header().Get("Content-Type"); contentType != "" {
			l.Infof("响应Content-Type: %s", contentType)
		}
		
		l.Infof("==================")
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

// isBinaryContentType 判断是否为二进制内容类型
func isBinaryContentType(contentType string) bool {
	if contentType == "" {
		return false
	}
	
	// 转换为小写进行比较
	contentType = strings.ToLower(contentType)
	
	// 定义二进制内容类型列表
	binaryTypes := []string{
		// 图片类型
		"image/",
		// 视频类型
		"video/",
		// 音频类型
		"audio/",
		// 应用程序二进制类型
		"application/octet-stream",
		"application/pdf",
		"application/zip",
		"application/x-rar-compressed",
		"application/x-7z-compressed",
		"application/x-tar",
		"application/gzip",
		// 字体文件
		"font/",
		"application/font-woff",
		"application/font-woff2",
		// 其他二进制格式
		"application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument",
		"application/msword",
	}
	
	// 检查是否匹配任何二进制类型
	for _, binaryType := range binaryTypes {
		if strings.HasPrefix(contentType, binaryType) {
			return true
		}
	}
	
	return false
}
