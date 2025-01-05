package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/tidwall/gjson"
)

type Marker struct {
	Id        int     `json:"id"`
	Name      string  `json:"name"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Rotate    int64   `json:"rotate"`
}

type Geometry struct {
	Type        string    `json:"type"`
	Coordinates []float64 `json:"coordinates"`
}

type Feature struct {
	Type       string   `json:"type"`
	Properties Marker   `json:"properties"`
	Geometry   Geometry `json:"geometry"`
}

func loadMarkersGeoJson(c *gin.Context) {
	s := `select id, name, latitude, longitude, rotate
			from t_photos where latitude is not null and trashed=false`
	var markers []Marker
	db().Raw(s).Find(&markers)
	var features []Feature
	for _, marker := range markers {
		features = append(features, Feature{
			Type:       "Feature",
			Properties: marker,
			Geometry: Geometry{
				Type:        "Point",
				Coordinates: []float64{marker.Longitude, marker.Latitude},
			},
		})
	}
	c.JSON(http.StatusOK, map[string]interface{}{
		"type":     "FeatureCollection",
		"features": features,
	})
}

func getAddress(latitude, longitude float64) string {
	// todo: takeToken();

	result, err := getAddressFromRemote(latitude, longitude)
	l.Info("qqlbs result:", result, err)
	if err == nil {
		return gjson.Get("result.address", result).String()
	}
	return ""
}

func getAddressFromRemote(latitude, longitude float64) (string, error) {
	requestStr := fmt.Sprintf("key=%s&location=%f,%f", viper.GetString("qqlbsKey"), latitude, longitude)
	sig := getMD5Hash("/ws/geocoder/v1?" + requestStr + viper.GetString("qqlbsToken"))
	url := fmt.Sprintf("https://apis.map.qq.com/ws/geocoder/v1?key=%s&location=%f%%2c%f&sig=%s", viper.GetString("qqlbsKey"), latitude, longitude, sig)
	l.Info("Getting gis info from " + url)
	// return (Map) Json.fromJson(Http.get(url).getContent());
	return getUrlResponse(url)
}
