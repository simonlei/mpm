package main

import (
	"context"
	"errors"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/tencentyun/cos-go-sdk-v5"
	"log"
	"net/http"
	"net/url"
	"strings"
)

func proxyCos(c *gin.Context) {
	// /cos/small/vpbts6incmgm7pr2osfh5lbkkm?imageMogr2/rotate/90
	key := c.Param("path")
	query := c.Request.URL.Query()
	var param string
	for k, _ := range query {
		param = k
	}
	log.Printf("key is %s params is %s\n", key, param)
	if strings.Contains(key, "/thumb") {
		if param != "" {
			c.Error(errors.New("同时存在param和thumb"))
		}
		key, param = parseKeyAndParam(key)
		log.Printf("parsed key is %s params is %s\n", key, param)
	}
	operation := ""
	if param != "" {
		operation = viper.GetString(param)
		if operation == "" {
			operation = param
		}
	}

	obj, err := Cos().CI.Get(context.Background(), key, operation, nil)
	if err != nil {
		c.Error(err)
	}

	c.DataFromReader(200, obj.ContentLength, obj.Response.Header.Get("content-type"), obj.Body, nil)
}

func parseKeyAndParam(key string) (string, string) {
	index := strings.Index(key, "/thumb")
	return key[:index], key[index+1:]
}

func Cos() *cos.Client {
	u, _ := url.Parse(fmt.Sprintf("https://%s.cos.%s.myqcloud.com", viper.GetString("cos.bucket"), viper.GetString("cos.region")))
	su, _ := url.Parse(fmt.Sprintf("https://cos.%s.myqcloud.com", viper.GetString("cos.region")))
	b := &cos.BaseURL{BucketURL: u, ServiceURL: su}
	// 1.永久密钥
	return cos.NewClient(b, &http.Client{
		Transport: &cos.AuthorizationTransport{
			SecretID:  viper.GetString("cos.secretId"),
			SecretKey: viper.GetString("cos.secretKey"),
		},
	})

}
