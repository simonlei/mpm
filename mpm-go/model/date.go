package model

import (
	"fmt"
	"time"
)

type Date time.Time

func (t Date) MarshalJSON() ([]byte, error) {
	var stamp = fmt.Sprintf("\"%s\"", time.Time(t).Format("2006-01-02"))
	return []byte(stamp), nil
}
func (t *Date) UnmarshalJSON(data []byte) error {
	var err error
	tt, err := time.ParseInLocation(`"2006-01-02 15:04:05"`, string(data), time.Local)
	if err != nil {
		return err
	}
	*t = Date(tt)
	return nil
}

type DateTime time.Time

func (t DateTime) MarshalJSON() ([]byte, error) {
	var stamp = fmt.Sprintf("\"%s\"", time.Time(t).Format("2006-01-02 15:04:05"))
	return []byte(stamp), nil
}
func (t *DateTime) UnmarshalJSON(data []byte) error {
	var err error
	tt, err := time.ParseInLocation(`"2006-01-02"`, string(data), time.Local)
	if err != nil {
		return err
	}
	*t = DateTime(tt)
	return nil
}

func (t DateTime) Year() int {
	d := time.Time(t)
	return d.Year()
}

func (t DateTime) Month() int {
	d := time.Time(t)
	return int(d.Month())
}
