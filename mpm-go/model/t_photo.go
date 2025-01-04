package model

// TPhoto 表示照片结构体
type TPhoto struct {
	ID          int64    `gorm:"column:id;primaryKey;autoIncrement" json:"id"` // 照片的唯一标识 ID，主键且自增
	Name        string   `gorm:"column:name;unique" json:"name"`               // 照片名称，唯一
	Size        int64    `gorm:"column:size" json:"size"`                      // 照片大小
	Width       int      `gorm:"column:width" json:"width"`                    // 照片宽度
	Height      int      `gorm:"column:height" json:"height"`                  // 照片高度
	MD5         string   `gorm:"column:md5" json:"md5"`                        // 照片的 MD5 值
	SHA1        string   `gorm:"column:sha1" json:"sha1"`                      // 照片的 SHA1 值
	Trashed     bool     `gorm:"column:trashed" json:"trashed"`                // 是否已被放入回收站
	Star        bool     `gorm:"column:star" json:"star"`                      // 是否被标记为星标
	Description string   `gorm:"column:description" json:"description"`        // 照片描述
	Latitude    float64  `gorm:"column:latitude" json:"latitude"`              // 拍摄地点的纬度
	Longitude   float64  `gorm:"column:longitude" json:"longitude"`            // 拍摄地点的经度
	Address     string   `gorm:"column:address" json:"address"`                // 拍摄地点的地址
	TakenDate   DateTime `gorm:"column:taken_date" json:"taken_date"`          // 拍摄日期时间
	MediaType   string   `gorm:"column:media_type" json:"media_type"`          // 类型，照片还是视频
	Duration    float64  `gorm:"column:duration" json:"duration"`              // 视频长度（仅当 MediaType 为 video 时有效）
	Rotate      int      `gorm:"column:rotate" json:"rotate"`                  // 旋转度数
	Tags        string   `gorm:"column:tags" json:"tags"`                      // 标签
	Activity    int64    `gorm:"column:activity" json:"activity"`              // 所属活动
	// 以下不在数据库中
	Thumb        string `gorm:"-" json:"thumb"`         // 缩略图
	TheYear      int    `gorm:"-" json:"the_year"`      // 年份
	TheMonth     int    `gorm:"-" json:"the_month"`     // 月份
	ActivityDesc string `gorm:"-" json:"activity_desc"` // 活动描述
}

func (*TPhoto) TableName() string {
	return "t_photos"
}
