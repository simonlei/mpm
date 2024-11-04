package main

import (
	"reflect"
	"testing"

	"github.com/gin-gonic/gin"
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
		want []*PhotoDate
	}{
		{
			name: "both false",
			want: []*PhotoDate{
				{Year: 2024, Month: 1, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2023, Month: 12, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2023, Month: 11, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2023, Month: 10, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2023, Month: 8, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2022, Month: 7, PhotoCount: 14, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2021, Month: 8, PhotoCount: 333, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2021, Month: 7, PhotoCount: 716, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2021, Month: 6, PhotoCount: 3, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2021, Month: 5, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2019, Month: 11, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2018, Month: 12, PhotoCount: 4, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2016, Month: 2, PhotoCount: 3, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2011, Month: 6, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 2000, Month: 12, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
				{Year: 1979, Month: 11, PhotoCount: 1, ActivityId: 0, Day: 0, Name: ""},
			},
		},
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
