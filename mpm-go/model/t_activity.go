package model

type TActivity struct {
	ID          int64   `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	Name        string  `gorm:"column:name;not null;comment:'活动名称'" json:"name"`
	Description string  `gorm:"column:description;not null;default:'';comment:'描述'" json:"description"`
	StartDate   Date    `gorm:"column:startDate;not null;comment:'开始日期'" json:"startDate"`
	EndDate     Date    `gorm:"column:endDate;not null;comment:'结束日期'" json:"endDate"`
	Latitude    float64 `gorm:"column:latitude;comment:'纬度'" json:"latitude"`
	Longitude   float64 `gorm:"column:longitude;comment:'经度'" json:"longitude"`
}

func (*TActivity) TableName() string {
	return "t_activity"
}
