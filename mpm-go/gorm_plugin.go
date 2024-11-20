package main

import (
	"runtime/debug"

	"gorm.io/gorm"
)

type ErrHandlePlugin struct {
}

var errHanlerPlugin ErrHandlePlugin
var namePrefix = " "

func (p ErrHandlePlugin) Name() string {
	return "gorm-plugin:ErrHandle"
}

// Initialize plugin
func (p ErrHandlePlugin) Initialize(db *gorm.DB) error {
	errHandler := func(gormDB *gorm.DB) {
		if gormDB.Error != nil && gormDB.Error != gorm.ErrRecordNotFound {
			l.Infof("SQL:" + gormDB.Statement.SQL.String() + " 执行异常:" + gormDB.Error.Error() + "\n 异常stack：" + string(debug.Stack()))
			gormDB.Rollback()
			panic("SQL:" + gormDB.Statement.SQL.String() + " 执行异常:" + gormDB.Error.Error() + "\n 异常stack：" + string(debug.Stack()))

		}
	}

	if err := db.Callback().Create().After("gorm:create").Register(namePrefix+"gorm:create", errHandler); err != nil {
		return err
	}

	if err := db.Callback().Delete().After("gorm:delete").Register(namePrefix+"gorm:delete", errHandler); err != nil {
		return err
	}

	if err := db.Callback().Query().After("gorm:query").Register(namePrefix+"gorm:query", errHandler); err != nil {
		return err
	}

	if err := db.Callback().Update().After("gorm:update").Register(namePrefix+"gorm:update", errHandler); err != nil {
		return err
	}

	if err := db.Callback().Row().After("gorm:row").Register(namePrefix+"gorm:row", errHandler); err != nil {
		return err
	}

	if err := db.Callback().Raw().After("gorm:raw").Register(namePrefix+"gorm:raw", errHandler); err != nil {
		return err
	}
	return nil
}
