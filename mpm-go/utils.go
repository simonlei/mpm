package main

import (
	"crypto/md5"
	"crypto/sha1"
	"fmt"
	"io"
	"math"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"
)

func getMD5Hash(s string) string {
	h := md5.New()
	io.WriteString(h, s)
	return fmt.Sprintf("%x", h.Sum(nil))
}

func getFileMD5(file *os.File) string {
	hash := md5.New()
	if _, err := io.Copy(hash, file); err != nil {
		panic(err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil))
}

func getFileSha1(file *os.File) string {
	hash := sha1.New()
	if _, err := io.Copy(hash, file); err != nil {
		panic(err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil))
}

func parseInt(s string) int {
	i, err := strconv.Atoi(s)
	if err != nil {
		return 0
	}
	return i
}

func parseInt64(s string) int64 {
	i64, err := strconv.ParseInt(s, 10, 0)
	if err != nil {
		return 0
	}
	return i64
}

func parseFloat(s string) float64 {
	f, err := strconv.ParseFloat(s, 64)
	if err != nil {
		return 0
	}
	return f
}

func convertToBytes(rc io.ReadCloser) []byte {
	defer rc.Close() // 确保在函数结束时关闭ReadCloser

	bytes, err := io.ReadAll(rc)
	if err != nil {
		return []byte{}
	}

	return bytes
}

func parseDate(str, pattern string) (time.Time, error) {
	date, err := time.Parse(pattern, str)
	if err != nil {
		return time.Time{}, fmt.Errorf("failed to parse date: %v", err)
	}
	return date, nil
}

func parseGps(str string) (float64, error) {
	strs := strings.Split(str, " ")
	if len(strs) != 3 {
		return 0, fmt.Errorf("invalid GPS format: %s", str)
	}

	var result float64
	for i, s := range strs {
		degrees, err := strconv.ParseFloat(s, 64)
		if err != nil {
			return 0, fmt.Errorf("failed to parse degrees: %v", err)
		}
		result += degrees / math.Pow(60, float64(i))
	}

	return result, nil
}

func getUrlResponse(url string) (string, error) {
	resp, err := http.Get(url)
	if err != nil {
		return "", fmt.Errorf("failed to make GET request: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read response body: %v", err)
	}

	return string(body), nil
}
