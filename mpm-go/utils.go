package main

import (
	"crypto/hmac"
	"crypto/md5"
	"crypto/sha1"
	"encoding/base32"
	"encoding/binary"
	"fmt"
	"io"
	"math"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
)

func getMD5Hash(s string) string {
	h := md5.New()
	io.WriteString(h, s)
	return fmt.Sprintf("%x", h.Sum(nil))
}

func getFileMD5(fileName string) string {
	if file, err := os.Open(fileName); err == nil {
		defer file.Close()
		hash := md5.New()
		if _, err := io.Copy(hash, file); err == nil {
			return fmt.Sprintf("%x", hash.Sum(nil))
		}
	}
	panic("Can't get md5")
}

func getFileSha1(fileName string) string {
	if file, err := os.Open(fileName); err == nil {
		defer file.Close()
		hash := sha1.New()
		if _, err := io.Copy(hash, file); err == nil {
			return fmt.Sprintf("%x", hash.Sum(nil))
		}
	}
	panic("Can't get sha1")
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

func safeGo(f func()) {
	go func() {
		defer func() {
			if r := recover(); r != nil {
				// 在这里处理panic，例如记录日志或发送通知
				l.Infof("Recovered from panic: %v", r)
			}
		}()
		f()
	}()
}

func totp(c *gin.Context) {
	secret := viper.GetString("totpSecretKey")
	interval := time.Now().Unix()
	r := generateTOTP(secret, interval)
	c.JSON(200, Response{0, r})
}

func generateTOTP(secret string, interval int64) string {
	if secret == "" || interval <= 0 {
		return ""
	}
	// 解码Base32编码的密钥
	key, _ := base32.StdEncoding.DecodeString(secret)

	// 计算时间戳
	counter := interval / 30
	buf := make([]byte, 8)
	binary.BigEndian.PutUint64(buf, uint64(counter))

	// HMAC-SHA1哈希
	h := hmac.New(sha1.New, key)
	h.Write(buf)
	hash := h.Sum(nil)

	// 动态截断
	offset := hash[19] & 0xf
	code := ((int(hash[offset]) & 0x7f) << 24) |
		((int(hash[offset+1]) & 0xff) << 16) |
		((int(hash[offset+2]) & 0xff) << 8) |
		(int(hash[offset+3]) & 0xff)

	// 生成6位数字
	return fmt.Sprintf("%06d", code%1000000)
}
