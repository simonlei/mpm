package main

import (
	"fmt"
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
	fmt.Println(base)
	r.Use(static.Serve("/", static.LocalFile(base, true)))
	r.POST("/api/getPicsDate", getPicsDate)
	r.POST("/api/getPics", getPics)
	r.POST("/api/getCount", getCount)
	r.POST("/api/getAllTags", getAllTags)
	r.GET("/cos/*path", cachecontrol.New(cachecontrol.CacheAssetsForeverPreset), proxyCos)

	configForward(r)

	r.Run(":18880")
}

func configForward(r *gin.Engine) {
	r.NoRoute(func(c *gin.Context) {
		remote, _ := url.Parse("http://localhost:" + getEnvIgnoreCaseWithDefault("SERVER_PORT", "8080"))
		fmt.Println(remote)
		director := func(req *http.Request) {
			req.URL.Scheme = remote.Scheme
			req.URL.Host = remote.Host
			req.URL.Path = remote.Path + c.Request.URL.Path
			req.Header = c.Request.Header
		}
		proxy := &httputil.ReverseProxy{Director: director}
		proxy.ServeHTTP(c.Writer, c.Request)
		fmt.Println("Forwarded request ", c.Request.URL)
	})
}
