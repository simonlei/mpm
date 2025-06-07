package main

import (
	"testing"

	"github.com/magiconair/properties/assert"
	"github.com/tidwall/gjson"
)

func Test_getGisAddress(t *testing.T) {
	tests := []struct {
		name string // description of this test case
		// Named input parameters for target function.
		latitude  float64
		longitude float64
		want      string
	}{{
		"",
		1.0, 1.0,
		"",
	},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := getGisAddress(tt.latitude, tt.longitude)
			// TODO: update the condition below to compare got with tt.want.
			if true {
				t.Errorf("getGisAddress() = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_getAddressFromRemote(t *testing.T) {
	tests := []struct {
		name string // description of this test case
		// Named input parameters for target function.
		latitude  float64
		longitude float64
		want      string
		wantErr   bool
	}{
		{
			"common",
			4.6497472222, 118.5494222222,
			"马来西亚沙巴Semporna",
			false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, gotErr := getAddressFromRemote(tt.latitude, tt.longitude)
			if gotErr != nil {
				if !tt.wantErr {
					t.Errorf("getAddressFromRemote() failed: %v", gotErr)
				}
				return
			}
			if tt.wantErr {
				t.Fatal("getAddressFromRemote() succeeded unexpectedly")
			}
			// TODO: update the condition below to compare got with tt.want.
			if tt.want != gjson.Get(got, "result.address").String() {
				t.Errorf("getAddressFromRemote() = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_GetResult(t *testing.T) {
	assert.Equal(t, "马来西亚沙巴Semporna", gjson.Get(`{"status":0,"message":"Success","request_id":"f4a8670574774942bb4acfac92b0841e","result":{"location":{"lat":4.649756,"lng":118.549422},"address":"马来西亚沙巴Semporna","address_component":{"nation":"马来西亚","ad_level_1":"沙巴","ad_level_2":"Semporna","ad_level_3":"","street":"","locality":""},"ad_info":{"nation_code":"458","city_code":"","location":{"lat":4.649756,"lng":118.549422}}}}`, "result.address").String())
}
