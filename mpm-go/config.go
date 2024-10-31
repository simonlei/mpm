package main

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

func init() {
	viper.SetConfigName("application")
	viper.SetConfigType("properties")
	viper.AddConfigPath("config/")
	viper.AddConfigPath("../mpm-server/src/main/resources") // for dev
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
}

func getEnvIgnoreCase(key string) string {
	env := os.Getenv(strings.ToLower(key))
	if env == "" {
		env = os.Getenv(strings.ToUpper(key))
	}
	return env
}

func getEnvIgnoreCaseWithDefault(key, dft string) string {
	env := getEnvIgnoreCase(key)
	if env == "" {
		return dft
	}
	return env
}
