package main

import (
	"context"
	"io"
	"net/url"
	"testing"

	"github.com/magiconair/properties/assert"
	"github.com/spf13/viper"
	"github.com/tencentyun/cos-go-sdk-v5"
)

func Test_proxyCos(t *testing.T) {
	u, err := url.Parse("https://example.org/cos/small/vpbts6incmgm7pr2osfh5lbkkm?imageMogr2/rotate/90")
	if err != nil {
		l.Fatal(err)
	}
	q := u.Query()
	var key string
	for k := range q {
		key = k
	}
	l.Info(key)
	l.Info(key == "")
}

func TestSubThumbnail(t *testing.T) {
	key := "/small/rpdsoktgq4hg3qd80q9ag0931u/thumb180"
	key, param := parseKeyAndParam(key)
	assert.Equal(t, "/small/rpdsoktgq4hg3qd80q9ag0931u", key)
	assert.Equal(t, "thumb180", param)
	key, param = parseKeyAndParam("/small/rpdsoktgq4hg3qd80q9ag0931u/thumb")
	assert.Equal(t, "/small/rpdsoktgq4hg3qd80q9ag0931u", key)
	assert.Equal(t, "thumb", param)

	assert.Equal(t, "imageMogr2/auto-orient/thumbnail/200x150/pad/1/color/IzNEM0QzRA", viper.GetString("thumb"))
}

func Test_Exif(t *testing.T) {
	obj, err := Cos().CI.Get(context.Background(), "origin/60991178-49fe-4616-aad7-4dd1332531d1", "exif", nil)
	t.Log(err)
	a, err := io.ReadAll(obj.Body)
	t.Log(err)
	t.Log(string(a))

	obj, err = Cos().CI.Get(context.Background(), "origin/60991178-49fe-4616-aad7-4dd1332531d1", "imageInfo", nil)
	t.Log(err)
	a, err = io.ReadAll(obj.Body)
	t.Log(err)
	t.Log(string(a))
}

func Test_MediaTemplate(t *testing.T) {
	result, resp, err := Cos().CI.DescribeMediaTemplate(context.Background(), &cos.DescribeMediaTemplateOptions{
		Tag:  "Transcode",
		Name: "video-converter",
	})
	t.Log(result)
	t.Log(resp)
	t.Log(err)
}

func Test_CreateTemplate(t *testing.T) {
	createConvertTemplate()
}
