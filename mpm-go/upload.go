package main

import (
	"io"
	"mpm-go/model"
	"os"
	"strings"
	"sync"

	"github.com/gin-gonic/gin"
)

func uploadPhoto(c *gin.Context) {
	lastModified := c.PostForm("lastModified")
	account := c.GetHeader("Account")
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		panic(err)
	}
	defer file.Close()
	l.Info("total files %d with %d, upload user: %s", header.Size, lastModified, account)
	key := "upload/" + getFullPathName(header.Header.Get("Content-Disposition"))
	tmpFile, err := os.CreateTemp("", "mpm*"+header.Filename)

	if err != nil {
		panic(err)
	}

	_, err = io.Copy(tmpFile, file)
	tmpFile.Close()

	if err != nil {
		panic(err)
	}

	defer func() {
		if recover() != nil {
			l.Error("Can't upload file:", key, err)
			db().Create(&model.Meta{
				Key:   "Error_Log_" + key,
				Value: err.Error(),
			})
		}
	}()

	uploadFileToCos(key, header.Header.Get("Content-Type"), header.Size, tmpFile.Name())
	uploadFile(key, lastModified, header.Header.Get("Content-Type"), header.Size, tmpFile.Name(), account)
	c.JSON(200, Response{Code: 0, Data: 0})
}

func getFullPathName(s string) string {
	ss := strings.Split(s, ";")
	for _, v := range ss {
		if strings.Contains(v, "filename") {
			sss := v[11 : len(v)-1]
			return sss
		}
	}
	return ""
}

func uploadFile(key, lastModified, contentType string, size int64, fileName string, uploadUser string) {
	file, _ := os.Open(fileName)
	defer file.Close()
	l.Info("Key is	", key)
	// upload/tmpupload/七上1025义工/IMG_002.jpg
	paths := strings.Split(key, "/")
	path := "/" + paths[1]
	// name: tmpupload
	parent := existOrCreate(nil, path, paths[1], true, uploadUser)
	// skip upload, tmpupload and last one
	if len(paths) > 2 {
		for i := 2; i < len(paths)-1; i++ {
			path += "/" + paths[i]
			// path exist?
			parent = existOrCreate(parent, path, paths[i], true, uploadUser)
		}
	}
	name := paths[len(paths)-1]
	photo := savePhotoInDb(key, name, lastModified, contentType, size, fileName)
	if photo != nil {
		tfile := existOrCreate(parent, path+"/"+name, name, false, uploadUser)
		tfile.PhotoID = photo.ID
		tfile.PhotoName = photo.Name
		db().Save(tfile)
	}
}

var fileMutex sync.Mutex

func existOrCreate(parent *model.TFile, path, name string, isFolder bool, uploadUser string) *model.TFile {
	// 增加一个锁，避免重复建相同path的file
	fileMutex.Lock()
	defer fileMutex.Unlock()

	var file model.TFile
	db().Where("path = ?", path).First(&file)
	if file.ID != 0 {
		// 如果文件已存在但没有上传用户信息，则更新
		if file.UploadUser == "" && uploadUser != "" {
			file.UploadUser = uploadUser
			db().Save(&file)
		}
		return &file
	}
	var parentId int64 = -1
	if parent != nil {
		parentId = parent.ID
	}
	file = model.TFile{
		Path:       path,
		Name:       name,
		IsFolder:   isFolder,
		ParentID:   parentId,
		UploadUser: uploadUser,
	}
	db().Create(&file)
	return &file
}
