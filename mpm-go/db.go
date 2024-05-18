package main

import (
	"fmt"
	"github.com/spf13/viper"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/schema"
	"log"
	"time"
)

var dbInstance *gorm.DB

func init() {
	log.Println("in db Init")
	source := fetchDbSource()
	log.Printf("start init mysql with %v \n", source)

	db, err := gorm.Open(mysql.Open(source), &gorm.Config{
		// Logger: tgorm.DefaultTRPCLogger,
		NamingStrategy: schema.NamingStrategy{
			SingularTable: true, // use singular table name, table for `User` would be `user` with this option enabled
		}})
	if err != nil {
		log.Println("DB Open error,err=", err.Error())
		// return err
	}

	sqlDB, err := db.DB()
	if err != nil {
		log.Println("DB Init error,err=", err.Error())
		// return err
	}

	// 用于设置连接池中空闲连接的最大数量
	sqlDB.SetMaxIdleConns(10)
	// 设置打开数据库连接的最大数量
	sqlDB.SetMaxOpenConns(200)
	// 设置了连接可复用的最大时间
	sqlDB.SetConnMaxLifetime(time.Minute * 10)

	dbInstance = db

	log.Println("finish init mysql with ", source)
	// return nil
}

func fetchDbSource() string {
	source := "%s:%s@tcp(127.0.0.1:3306)/%s?readTimeout=1500ms&writeTimeout=1500ms&charset=utf8mb4&loc=Local&&parseTime=true"
	user := viper.Get("jdbc.username")
	log.Printf("user is %v\n", user)
	pwd := viper.Get("jdbc.password")
	dataBase := "photohome"
	source = fmt.Sprintf(source, user, pwd, dataBase)
	return source
}

func db() *gorm.DB {
	return dbInstance
}
