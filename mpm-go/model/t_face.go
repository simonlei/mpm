package model

type TFace struct {
	ID           int64  `gorm:"column:id;type:bigint(20);primaryKey;autoIncrement" json:"id"`
	Name         string `gorm:"column:name;type:varchar(100);default:null" json:"name"`
	PersonId     string `gorm:"column:person_id;type:varchar(100);default:null" json:"person_id"`
	FaceId       int64  `gorm:"column:face_id;type:bigint(20);default:null" json:"face_id"`
	SelectedFace int64  `gorm:"column:selected_face;type:bigint(20);default:null;comment:'选择用的头像，默认是空，用最大的头像'" json:"selected_face"`
	Collected    bool   `gorm:"column:collected;type:int(11);default:0;comment:'是否收藏'" json:"collected"`
	Hidden       bool   `gorm:"column:hidden;type:int(11);default:0;comment:'是否隐藏'" json:"hidden"`
}
