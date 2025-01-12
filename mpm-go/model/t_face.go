package model

type TFace struct {
	ID           int64  `gorm:"type:bigint(20);primary_key;auto_increment" json:"id"`
	Name         string `gorm:"type:varchar(100);default:null" json:"name"`
	PersonId     string `gorm:"type:varchar(100);default:null" json:"personId"`
	FaceId       int64  `gorm:"type:bigint(20);default:null" json:"faceId"`
	SelectedFace int64  `gorm:"type:bigint(20);default:null;comment:'选择用的头像，默认是空，用最大的头像'" json:"selectedFace"`
	Collected    bool   `gorm:"type:int(11);default:0;comment:'是否收藏'" json:"collected"`
	Hidden       bool   `gorm:"type:int(11);default:0;comment:'是否隐藏'" json:"hidden"`
}
