package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

type PicDateRequest struct {
	Trashed, Star bool
}

func getPicsDate(c *gin.Context) {
	var req PicDateRequest
	err := c.BindJSON(&req)
	if err != nil {
		c.String(http.StatusBadRequest, "Bad request %v", err)
		return
	}
	l.Infof("req %v", req)
	dates := getPhotoDates(req.Trashed, req.Star)
	var months = make(map[int]*TreeNode)
	result := addYearAndMonths(dates, months)

	activities := getActivities(req.Trashed, req.Star)
	addActivities(activities, months)
	c.JSON(http.StatusOK, Response{0, result})
}

func addActivities(activities []*PhotoDate, months map[int]*TreeNode) {
	for _, a := range activities {
		month, ok := months[(a.Year*100 + a.Month)]
		if !ok {
			continue
		}
		activity := TreeNode{
			Id:         1000000 + a.ActivityId,
			Year:       month.Year,
			Month:      month.Month,
			PhotoCount: a.PhotoCount,
			Title:      fmt.Sprintf("%d月%d日-%s(%d)", a.Month, a.Day, a.Name, a.PhotoCount),
		}
		month.Children = append(month.Children, &activity)
	}
}

func getActivities(trashed bool, star bool) []*PhotoDate {
	sql := `select year(start_date) as year, month(start_date) as month, t_activity.id as activity_id,
                  count(t_photos.id) as photo_count, day(start_date) as day, t_activity.name as name
                from t_activity
                left join t_photos on t_photos.activity=t_activity.id
                where trashed = %t %s
                group by year, month, activity_id
                order by year desc, month desc, start_date`
	return queryDates(trashed, star, sql)
}

func addYearAndMonths(dates []*PhotoDate, months map[int]*TreeNode) []*TreeNode {
	var lastYear *TreeNode
	yearCount := 0
	var result []*TreeNode

	for _, d := range dates {
		if lastYear == nil {
			lastYear = &TreeNode{
				Year: d.Year,
				Id:   d.Year,
			}
			result = append(result, lastYear)
		}
		if lastYear.Year != d.Year {
			lastYear.Title = fmt.Sprintf("%d年(%d)", lastYear.Year, yearCount)
			yearCount = 0
			lastYear = &TreeNode{
				Year: d.Year,
				Id:   d.Year,
			}
			result = append(result, lastYear)
		}
		month := TreeNode{
			Id:         lastYear.Year*100 + d.Month,
			Year:       lastYear.Year,
			Month:      d.Month,
			PhotoCount: d.PhotoCount,
			Title:      fmt.Sprintf("%d月(%d)", d.Month, d.PhotoCount),
		}
		months[month.Id] = &month
		yearCount += d.PhotoCount
		lastYear.Children = append(lastYear.Children, &month)
	}
	if lastYear != nil {
		lastYear.Title = fmt.Sprintf("%d年(%d)", lastYear.Year, yearCount)
	}
	return result
}

type TreeNode struct {
	Id         int         `json:"id"`
	Year       int         `json:"year"`
	Month      int         `json:"month"`
	PhotoCount int         `json:"photoCount"`
	Title      string      `json:"title"`
	Children   []*TreeNode `json:"children"`
}

type PhotoDate struct {
	Year       int
	Month      int
	PhotoCount int
	ActivityId int
	Day        int
	Name       string
}

func getPhotoDates(trashed bool, star bool) []*PhotoDate {
	sql := `select year(taken_date) as year, month(taken_date) as month, count(*) as photo_count
	           from t_photos
	           where trashed = %t %s
	           group by year, month
	           order by year desc, month desc`
	return queryDates(trashed, star, sql)
}

func queryDates(trashed bool, star bool, sql string) []*PhotoDate {
	stared := ""
	if star {
		stared = " and star = true "
	}
	var dates []*PhotoDate
	tx := db().Raw(fmt.Sprintf(sql, trashed, stared)).Scan(&dates)
	if tx.Error != nil {
		l.Infof("getPhotoDates: %v", tx.Error)
	}
	for _, d := range dates {
		l.Infof("dates %v", d)
	}
	return dates
}
