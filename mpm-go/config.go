package main

import (
	"os"
	"strings"
)

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
