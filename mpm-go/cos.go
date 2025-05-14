package main

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"net/url"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/tencentcloud/tencentcloud-sdk-go/tencentcloud/common"
	"github.com/tencentcloud/tencentcloud-sdk-go/tencentcloud/common/profile"
	v20200303 "github.com/tencentcloud/tencentcloud-sdk-go/tencentcloud/iai/v20200303"
	"github.com/tencentyun/cos-go-sdk-v5"
)

func proxyCos(c *gin.Context) {
	// /cos/small/vpbts6incmgm7pr2osfh5lbkkm?imageMogr2/rotate/90
	key := c.Param("path")
	query := c.Request.URL.Query()
	var param string
	for k := range query {
		param = k
	}
	l.Infof("key is %s params is %s", key, param)
	if strings.Contains(key, "/thumb") {
		if param != "" {
			c.Error(errors.New("同时存在param和thumb"))
		}
		key, param = parseKeyAndParam(key)
		l.Infof("parsed key is %s params is %s", key, param)
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
	ci, _ := url.Parse(fmt.Sprintf("https://%s.ci.%s.myqcloud.com", viper.GetString("cos.bucket"), viper.GetString("cos.region")))
	b := &cos.BaseURL{
		BucketURL:  u,
		ServiceURL: su,
		CIURL:      ci}
	// 1.永久密钥
	return cos.NewClient(b, &http.Client{
		Transport: &cos.AuthorizationTransport{
			SecretID:  viper.GetString("cos.secretId"),
			SecretKey: viper.GetString("cos.secretKey"),
		},
	})

}

func uploadFileToCos(key, contentType string, size int64, fileName string) {
	file, _ := os.Open(fileName)
	defer file.Close()
	uopt := cos.ObjectPutOptions{
		ObjectPutHeaderOptions: &cos.ObjectPutHeaderOptions{
			ContentType:   contentType,
			ContentLength: size,
		},
	}
	resp, err := Cos().Object.Put(context.Background(), key, file, &uopt)
	if err != nil {
		panic(err)
	}
	l.Info("upload result ", resp.Body)
}

func Iai() *v20200303.Client {
	cred := &common.Credential{
		SecretId:  viper.GetString("cos.secretId"),
		SecretKey: viper.GetString("cos.secretKey"),
	}
	prof := profile.NewClientProfile()
	prof.HttpProfile.Endpoint = "iai.tencentcloudapi.com"

	cli, err := v20200303.NewClient(cred, viper.GetString("cos.region"), prof)
	if err != nil {
		panic(err)
	}
	return cli
}
