package main

import (
	"reflect"
	"testing"
)

func Test_getTagsFromDb(t *testing.T) {
	tests := []struct {
		name string
		want []string
	}{
		{
			"nothing",
			[]string{"simon"},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := getTagsFromDb(); !reflect.DeepEqual(got, tt.want) {
				t.Errorf("getTagsFromDb() = %v, want %v", got, tt.want)
			}
		})
	}
}
