package model

import (
	"time"
)

// Meta 表示元数据结构体
type Meta struct {
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"` // 元数据的唯一标识 ID，主键且自增
	Key       string    `gorm:"column:c_key;unique" json:"c_key"`             // 元数据键，唯一
	Value     string    `gorm:"column:c_value" json:"c_value"`                // 元数据值
	CreatedAt time.Time `gorm:"column:createdAt;autoCreateTime" json:"createdAt"`
	UpdatedAt time.Time `gorm:"column:updatedAt;autoUpdateTime" json:"updatedAt"`
}

func (*Meta) TableName() string {
	return "t_metas"
}
