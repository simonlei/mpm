package model

import "gorm.io/gorm"

type PhotoFaceInfo struct {
	gorm.Model
	PhotoId int64 `gorm:"column:photoId" json:"photoId"`
	FaceId  int64 `gorm:"column:faceId" json:"faceId"`
	X       int   `gorm:"column:x" json:"x"`
	Y       int   `gorm:"column:y" json:"y"`
	Width   int   `gorm:"column:width" json:"width"`
	Height  int   `gorm:"column:height" json:"height"`
}
