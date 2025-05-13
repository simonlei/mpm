package main

import (
	"fmt"

	"github.com/spf13/viper"
	"go.uber.org/zap"
)

var l *zap.SugaredLogger

func init() {
	viper.SetConfigName("application")
	viper.SetConfigType("properties")
	viper.AddConfigPath("config/")
	viper.AddConfigPath("../docs") // for dev
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Sprintf("Fatal error config file: %s ", err))
	}
	viper.SetConfigName("cos_styles")
	viper.SetConfigType("properties")
	err = viper.MergeInConfig()
	if err != nil {
		panic(fmt.Sprintf("Fatal error config file: %s ", err))
	}
	lo, _ := zap.NewDevelopment()
	l = lo.Sugar()
	defer l.Sync()
}
