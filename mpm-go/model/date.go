package model

import (
	"database/sql/driver"
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

// Value方法将自定义日期类型转换为数据库能够存储的格式，这里转换为time.Time类型
func (d Date) Value() (driver.Value, error) {
	t := time.Time(d)
	return t, nil
}

// Scan方法将从数据库读取的数据（通常是time.Time类型）转换为自定义日期类型
func (d *Date) Scan(value interface{}) error {
	if value == nil {
		return nil
	}
	t, ok := value.(time.Time)
	if !ok {
		return fmt.Errorf("无法将值转换为time.Time类型")
	}
	*d = Date(t)
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

func (dt *DateTime) Scan(value interface{}) error {
	if value == nil {
		return nil
	}
	switch v := value.(type) {
	case time.Time:
		*dt = DateTime(v)
	default:
		return fmt.Errorf("unsupported scan type for DateTime: %T", value)
	}
	return nil
}

func (dt DateTime) Value() (driver.Value, error) {
	return time.Time(dt), nil
}
