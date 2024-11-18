package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
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
