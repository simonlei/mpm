package model

type TBlockPhoto struct {
	ID   int64  `gorm:"column:id;primaryKey;autoIncrement" json:"id"` // 唯一标识 ID，主键且自增
	Size int64  `gorm:"column:size;type:bigint(20);default:NULL" json:"size"`
	MD5  string `gorm:"column:md5;type:varchar(128);default:NULL" json:"md5"`
	SHA1 string `gorm:"column:sha1;type:varchar(128);default:NULL" json:"sha1"`
}

func (TBlockPhoto) TableName() string {
	return "t_block_photos"
}
