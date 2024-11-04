package main

import (
	"fmt"
	"time"

	"github.com/spf13/viper"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/schema"
)

var dbInstance *gorm.DB

func init() {
	l.Info("in db Init")
	source := fetchDbSource()
	l.Infof("start init mysql with %v", source)

	db, err := gorm.Open(mysql.Open(source), &gorm.Config{
		// Logger: tgorm.DefaultTRPCLogger,
		NamingStrategy: schema.NamingStrategy{
			SingularTable: true, // use singular table name, table for `User` would be `user` with this option enabled
		}})
	if err != nil {
		l.Info("DB Open error,err=", err.Error())
		// return err
	}

	sqlDB, err := db.DB()
	if err != nil {
		l.Info("DB Init error,err=", err.Error())
		// return err
	}

	// 用于设置连接池中空闲连接的最大数量
	sqlDB.SetMaxIdleConns(10)
	// 设置打开数据库连接的最大数量
	sqlDB.SetMaxOpenConns(200)
	// 设置了连接可复用的最大时间
	sqlDB.SetConnMaxLifetime(time.Minute * 10)

	dbInstance = db

	l.Infof("finish init mysql with %s", source)
	// return nil
}

func fetchDbSource() string {
	source := "%s:%s@tcp(localhost:3306)/%s?readTimeout=1500ms&writeTimeout=1500ms&charset=utf8mb4&loc=Local&&parseTime=true"
	user := viper.Get("jdbc.username")
	l.Infof("user is %v", user)
	pwd := viper.Get("jdbc.password")
	dataBase := "photohome"
	source = fmt.Sprintf(source, user, pwd, dataBase)
	return source
}

func db() *gorm.DB {
	return dbInstance
}

type Date time.Time

func (t Date) MarshalJSON() ([]byte, error) {
	var stamp = fmt.Sprintf("\"%s\"", time.Time(t).Format("2006-01-02"))
	return []byte(stamp), nil
}
func (t *Date) UnmarshalJSON(data []byte) error {
	var err error
	tt, err := time.ParseInLocation(`"2006-01-02"`, string(data), time.Local)
	if err != nil {
		return err
	}
	*t = Date(tt)
	return nil
}
