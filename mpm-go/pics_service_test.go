package main

import (
	"mpm-go/model"
	"testing"

	"github.com/magiconair/properties/assert"
)

func Test_setInfosFromCos(t *testing.T) {
	p := &model.TPhoto{}
	tests := []struct {
		name string // description of this test case
		// Named input parameters for target function.
		key   string
		photo *model.TPhoto
	}{
		{
			"pic with empty gis",
			"origin/60991178-49fe-4616-aad7-4dd1332531d1",
			p,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			setInfosFromCos(tt.key, tt.photo)
			assert.Equal(t, 1836, p.Width)
			assert.Equal(t, 4080, p.Height)
			assert.Equal(t, 0, p.Longitude)
			assert.Equal(t, 0, p.Latitude)
		})
	}
}
