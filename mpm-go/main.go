package main

import (
	"fmt"
	"github.com/gin-contrib/static"
	"github.com/gin-gonic/gin"
	"net/http"
	"net/http/httputil"
	"net/url"
)

func main() {
	setupEngine()
}

func setupEngine() {
	r := gin.New()

	base := getEnvIgnoreCase("static_base") // ../mpm-vue3/dist/
	fmt.Println(base)
	r.Use(static.Serve("/", static.LocalFile(base, true)))

	r.NoRoute(func(c *gin.Context) {
		remote, _ := url.Parse("http://localhost:" + getEnvIgnoreCaseWithDefault("SERVER_PORT", "80"))
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

	r.Run(":8090")
}
