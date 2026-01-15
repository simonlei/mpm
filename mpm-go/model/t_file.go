package model

import "time"

// TFile 表示文件结构体
type TFile struct {
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"` // 文件的唯一标识 ID，主键且自增
	Name      string    `gorm:"column:name" json:"name"`                      // 文件名称
	IsFolder  bool      `gorm:"column:is_folder" json:"is_folder"`            // 是否为文件夹
	ParentID  int64     `gorm:"column:parent_id" json:"parent_id"`            // 父文件夹 ID
	PhotoID   int64     `gorm:"column:photo_id" json:"photo_id"`              // 照片 ID
	PhotoName string    `gorm:"column:photo_name" json:"photo_name"`          // 照片名称
	Path      string    `gorm:"column:path" json:"path"`                      // 文件路径
	CreatedAt time.Time `gorm:"column:created_at;autoCreateTime" json:"created_at"`
	// UpdatedAt time.Time `gorm:"column:updatedAt;autoUpdateTime" json:"updatedAt"`
}

func (*TFile) TableName() string {
	return "t_files"
}
