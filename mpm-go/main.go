package main

import (
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/gin-contrib/static"
	"github.com/gin-gonic/gin"
	cachecontrol "go.eigsys.de/gin-cachecontrol/v2"
)

func main() {
	setupEngine()
}

func setupEngine() {
	r := gin.New()

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
