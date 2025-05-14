package model

type TFace struct {
	ID           int64  `gorm:"column:id;type:bigint(20);primaryKey;autoIncrement" json:"id"`
	Name         string `gorm:"column:name;type:varchar(100);default:null" json:"name"`
	PersonId     string `gorm:"column:personId;type:varchar(100);default:null" json:"personId"`
	FaceId       int64  `gorm:"column:faceId;type:bigint(20);default:null" json:"faceId"`
	SelectedFace int64  `gorm:"column:selectedFace;type:bigint(20);default:null;comment:'选择用的头像，默认是空，用最大的头像'" json:"selectedFace"`
	Collected    bool   `gorm:"column:collected;type:int(11);default:0;comment:'是否收藏'" json:"collected"`
	Hidden       bool   `gorm:"column:hidden;type:int(11);default:0;comment:'是否隐藏'" json:"hidden"`
}
