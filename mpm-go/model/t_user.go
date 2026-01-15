package model

type TUser struct {
	ID        int64  `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	Account   string `gorm:"column:account" json:"account"`
	Name      string `gorm:"column:name" json:"name"`
	Salt      string `gorm:"column:salt" json:"salt"`
	Passwd    string `gorm:"column:passwd" json:"passwd"`
	FaceId    int64  `gorm:"column:face_id" json:"face_id"`
	IsAdmin   *bool  `gorm:"column:is_admin" json:"is_admin"`
	Signature string `gorm:"-" json:"signature"`
}

func (*TUser) TableName() string {
	return "t_users"
}
