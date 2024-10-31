package main

import (
	"fmt"
	"github.com/magiconair/properties/assert"
	"github.com/spf13/viper"
	"log"
	"net/url"
	"testing"
)

func Test_proxyCos(t *testing.T) {
	u, err := url.Parse("https://example.org/cos/small/vpbts6incmgm7pr2osfh5lbkkm?imageMogr2/rotate/90")
	if err != nil {
		log.Fatal(err)
	}
	q := u.Query()
	var key string
	for k := range q {
		key = k
	}
	fmt.Println(key)
	fmt.Println(key == "")
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
