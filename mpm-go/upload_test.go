package main

import (
	"os"
	"testing"

	"github.com/evanoberholster/imagemeta"
	"github.com/magiconair/properties/assert"
)

func Test_FullPath(t *testing.T) {
	s := `form-data; name="file"; filename="tmp/icon_ne4.png"`
	assert.Equal(t, "tmp/icon_ne4.png", getFullPathName(s))
}

func Test_UploadFile(t *testing.T) {
	uploadFileToCos("upload/1736081374298_tmp/icon04.png", "image/png", 55908, "/tmp/mpm3038335038icon04.png")
	uploadFile("upload/1736081374298_tmp/icon04.png", "1656238974000", "image/png", 55908, "/tmp/mpm3038335038icon04.png")
}

func Test_FileExif(t *testing.T) {
	file, err := os.Open("/tmp/mpm2878391466icon_ne4.png")
	t.Log(err)
	exif, err := imagemeta.Decode(file)
	t.Log(err)
	t.Log(exif)
}
