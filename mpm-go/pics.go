package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"log"
	"net/http"
)

type PicDateRequest struct {
	trashed, star bool
}

func getPicsDate(c *gin.Context) {
	var req PicDateRequest
	err := c.BindJSON(&req)
	if err != nil {
		c.String(http.StatusBadRequest, "Bad request %v", err)
		return
	}
	log.Printf("req %v", req)
	dates := getPhotoDates(req.trashed, req.star)
	var months = make(map[int]*TreeNode)
	result := addYearAndMonths(dates, months)
	c.JSON(http.StatusOK, result)
	/*
	   log.info("Req is {}", req);
	   List<TreeNode> result = new ArrayList<>();
	   List<NutMap> photoDates = getPhotoDates(req.getTrashed(), req.getStar());
	   Map<Integer, TreeNode> monthMaps = new HashMap<>();
	   addYearAndMonths(photoDates, result, monthMaps);
	   List<NutMap> activities = getActivities(req.getTrashed(), req.getStar());
	   addActivities(activities, monthMaps);
	   return result;
	*/
}

func addYearAndMonths(dates []*PhotoDate, months map[int]*TreeNode) []*TreeNode {
	var lastYear TreeNode
	yearCount := 0
	var result []*TreeNode

	for _, d := range dates {
		if lastYear.id == 0 {
			lastYear = TreeNode{
				year: d.year,
				id:   d.year,
			}
			result = append(result, &lastYear)
		}
		if lastYear.year != d.year {
			lastYear.title = fmt.Sprintf("%d年(%d)", lastYear.year, yearCount)
			yearCount = 0
			lastYear = TreeNode{
				year: d.year,
				id:   d.year,
			}
			result = append(result, &lastYear)
		}
		month := TreeNode{
			id:         lastYear.year*100 + d.month,
			year:       lastYear.year,
			month:      d.month,
			photoCount: d.photoCount,
			title:      fmt.Sprintf("%d月(%d)", d.month, d.photoCount),
		}
		months[month.id] = &month
		yearCount += d.photoCount
		lastYear.children = append(lastYear.children, &month)
	}
	if lastYear.id > 0 {
		lastYear.title = fmt.Sprintf("%d年(%d)", lastYear.year, yearCount)
	}
	return result
}

type TreeNode struct {
	id         int         `json:"id"`
	year       int         `json:"year"`
	month      int         `json:"month"`
	photoCount int         `json:"photoCount"`
	title      string      `json:"title"`
	children   []*TreeNode `json:"children"`
}

type PhotoDate struct {
	year       int
	month      int
	photoCount int
}

func getPhotoDates(trashed bool, star bool) []*PhotoDate {
	stared := ""
	if star {
		stared = " star = true "
	}
	var dates []*PhotoDate

	rows, err := db().Raw(fmt.Sprintf(`select year(takenDate) as year, month(takenDate) as month, count(*) as photoCount
	           from t_photos
	           where trashed = %t %s
	           group by year, month
	           order by year desc, month desc
		`, trashed, stared)).Rows()
	// log.Println(tx.Statement.SQL)
	if err != nil {
		log.Printf("getPhotoDates error: %v", err)
	}
	defer rows.Close()
	for rows.Next() {
		var year, month, photoCount int
		rows.Scan(&year, &month, &photoCount)
		log.Printf("%d, %d, %d", year, month, photoCount)
		dates = append(dates, &PhotoDate{year, month, photoCount})
	}
	return dates
}
