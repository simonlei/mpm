package main

import (
	"github.com/gin-gonic/gin"
	"reflect"
	"testing"
)

func Test_addYearAndMonths(t *testing.T) {
	type args struct {
		dates  []*PhotoDate
		months map[int]*TreeNode
	}
	tests := []struct {
		name string
		args args
		want []*TreeNode
	}{
		// TODO: Add test cases.
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := addYearAndMonths(tt.args.dates, tt.args.months); !reflect.DeepEqual(got, tt.want) {
				t.Errorf("addYearAndMonths() = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_getPhotoDates(t *testing.T) {
	type args struct {
		trashed bool
		star    bool
	}
	tests := []struct {
		name string
		args args
		want []PhotoDate
	}{
		{name: "both false"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := getPhotoDates(tt.args.trashed, tt.args.star); !reflect.DeepEqual(got, tt.want) {
				t.Errorf("getPhotoDates() = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_getPicsDate(t *testing.T) {
	type args struct {
		c *gin.Context
	}
	tests := []struct {
		name string
		args args
	}{
		// TODO: Add test cases.
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			getPicsDate(tt.args.c)
		})
	}
}
