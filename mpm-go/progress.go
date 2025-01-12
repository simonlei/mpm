package main

import "github.com/google/uuid"

type ProgressInterface interface {
	Progress() float64
	Run()
}

type BaseProgress struct {
	Total int
	Count int
}

func (e BaseProgress) Progress() float64 {
	return float64(e.Count) / float64(e.Total)
}

var tasks map[string]ProgressInterface

func addTask(progress ProgressInterface) string {
	taskId := uuid.New().String()
	tasks[taskId] = progress
	return taskId
}
