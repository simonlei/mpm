package model

type PhotoFaceInfo struct {
	ID      int64 `gorm:"column:id;primaryKey;autoIncrement" json:"id"` // 元数据的唯一标识 ID，主键且自增
	PhotoId int64 `gorm:"column:photo_id" json:"photo_id"`
	FaceId  int64 `gorm:"column:face_id" json:"face_id"`
	X       int   `gorm:"column:x" json:"x"`
	Y       int   `gorm:"column:y" json:"y"`
	Width   int   `gorm:"column:width" json:"width"`
	Height  int   `gorm:"column:height" json:"height"`
}
