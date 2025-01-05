package main

import (
	"context"
	"encoding/json"
	"fmt"
	"mpm-go/model"
	"os"
	"strings"
	"time"

	"github.com/evanoberholster/imagemeta"
	"github.com/google/uuid"
	"github.com/spf13/viper"
	"github.com/tencentyun/cos-go-sdk-v5"
	"github.com/tidwall/gjson"
)

func getMajorName(filename string) string {
	// 使用strings.LastIndex获取最后一个点的位置
	index := strings.LastIndex(filename, ".")
	if index == -1 {
		// 如果没有点，则返回整个文件名
		return filename
	}
	// 返回点之前的部分
	return filename[:index]
}

func savePhotoInDb(key, name, lastModified, contentType string, size int64, file *os.File) *model.TPhoto {
	if strings.Contains(contentType, "video") {
		return saveVideo(file, size, lastModified, key, getMajorName(name))
	} else {
		return saveImage(file, size, lastModified, key, getMajorName(name))
	}
}

func saveImage(file *os.File, size int64, lastModified, key, name string) *model.TPhoto {
	photo := sameFileExist(file, key, name, size)
	if photo.ID > 0 {
		return photo
	}
	photo.MediaType = "photo"
	photo.Description = name
	setInfosFromCos(key, photo)
	setInfosFromFile(file, lastModified, photo)
	checkInBlacklist(photo)
	db().Create(&photo)
	l.Info("Photo:", photo)
	savePhotosOnCos(key, photo)
	return photo
}

func savePhotosOnCos(key string, photo *model.TPhoto) {
	_, _, err := Cos().Object.Copy(context.Background(), "origin/"+photo.Name,
		fmt.Sprintf("%s.cos.%s.myqcloud.com/%s", viper.GetString("cos.bucket"), viper.GetString("cos.region"), key), nil)
	if err == nil {
		generateSmallPic(key, photo.Name)
		Cos().Object.Delete(context.Background(), key)
	} else {
		panic(err)
	}
}

func generateSmallPic(key, name string) {
	rule := "imageMogr2/thumbnail/2560x1440"
	if viper.GetString("smallphoto.format") != "" {
		rule += "|imageMogr2/format/" + viper.GetString("smallphoto.format")
	}
	result, _, err := Cos().CI.ImageProcess(context.Background(), key, &cos.ImageProcessOptions{
		IsPicInfo: 1,
		Rules: []cos.PicOperationsRules{
			{
				Bucket: viper.GetString("cos.bucket"),
				FileId: "/small/" + name,
				Rule:   rule,
			},
		},
	})
	if err != nil {
		l.Error("Can't generate small pic", err)
	} else {
		l.Info("Generate small pic result", result)
		// format := result.ProcessResults[0].Format
		// TODO 没找到更新meta的go api实现
		/*
		   CIUploadResult result = cosClient.processImage(request);
		   String format = result.getProcessResults().getObjectList().get(0).getFormat();
		   ObjectMetadata metadata = new ObjectMetadata();
		   metadata.setContentType("image/" + format);
		   cosClient.updateObjectMetaData(bucket, "small/" + name, metadata);
		   log.info("ci upload result: " + Json.toJson(result));
		*/
	}
}

func setInfosFromFile(file *os.File, lastModified string, photo *model.TPhoto) {
	if time.Time(photo.TakenDate).IsZero() {
		photo.TakenDate = model.DateTime(time.UnixMilli(parseInt64(lastModified)))
	}
	exif, err := imagemeta.Decode(file)
	if err != nil {
		l.Error("Can't read exif info", err)
	} else {
		if !exif.DateTimeOriginal().IsZero() {
			photo.TakenDate = model.DateTime(exif.DateTimeOriginal())
		}
		if exif.GPS.Latitude() != 0 && exif.GPS.Longitude() != 0 {
			photo.Latitude = exif.GPS.Latitude()
			photo.Longitude = exif.GPS.Longitude()
			photo.Address = getGisAddress(photo.Latitude, photo.Longitude)
		}
	}
}

type ImageInfo struct {
	Format        string `json:"format"`
	Width         string `json:"width"`
	Height        string `json:"height"`
	Size          string `json:"size"`
	Md5           string `json:"md5"`
	PhotoRgb      string `json:"photo_rgb"`
	FrameCount    string `json:"frame_count"`
	BitDepth      string `json:"bit_depth"`
	VerticalDpi   string `json:"vertical_dpi"`
	HorizontalDpi string `json:"horizontal_dpi"`
}

func setInfosFromCos(key string, photo *model.TPhoto) {
	resp, err := Cos().Object.Get(context.Background(), key+"?imageInfo", nil)
	if err != nil {
		l.Error("Can't get image info", err)
	} else {
		var info ImageInfo
		json.Unmarshal(convertToBytes(resp.Body), &info)
		photo.Width = parseInt(info.Width)
		photo.Height = parseInt(info.Height)
	}
	resp, err = Cos().Object.Get(context.Background(), key+"?exif", nil)
	if err != nil {
		l.Error("Can't get exif info", err)
	} else {
		exif := string(convertToBytes(resp.Body))
		dateTimeVal := gjson.Get(exif, "DateTime.val").String()
		if dateTimeVal != "" {
			t, err := parseDate(dateTimeVal, "2006:01:02 15:04:05")
			if err == nil {
				photo.TakenDate = model.DateTime(t)
			}
		}
		lat := gjson.Get(exif, "GPSLatitude.val").String()
		if lat != "" {
			l, err := parseGps(lat)
			if err == nil {
				photo.Latitude = l
			}
		}
		lon := gjson.Get(exif, "GPSLongitude.val").String()
		if lon != "" {
			l, err := parseGps(lon)
			if err == nil {
				photo.Longitude = l
			}
		}
	}
}

func saveVideo(file *os.File, size int64, lastModified, key, name string) *model.TPhoto {
	video := sameFileExist(file, key, name, size)
	if video.ID > 0 {
		return video
	}
	video.MediaType = "video"
	checkInBlacklist(video)
	db().Create(&video)
	generatePoster(key, video)
	getVideoMetadata(key, video)
	// TODO: 应该有办法获取到video的拍摄时间吧？
	video.TakenDate = model.DateTime(time.UnixMilli(parseInt64(lastModified)))
	db().Save(&video)
	_, _, err := Cos().Object.Copy(context.Background(), "video/"+video.Name+".mp4",
		fmt.Sprintf("%s.cos.%s.myqcloud.com/%s", viper.GetString("cos.bucket"), viper.GetString("cos.region"), key), nil)
	if err == nil {
		Cos().Object.Delete(context.Background(), key)
	}
	startVideoConvertTask(video.Name)
	return video
}

func startVideoConvertTask(name string) {
	templateId := checkConvertTemplate()
	_, resp, _ := Cos().CI.CreateMediaJobs(context.Background(), &cos.CreateMediaJobsOptions{
		Tag: "Transcode",
		Operation: &cos.MediaProcessJobOperation{
			TemplateId: templateId,
			Output: &cos.JobOutput{
				Region: viper.GetString("cos.region"),
				Bucket: viper.GetString("cos.bucket"),
				Object: "video_t/" + name + ".mp4",
			},
		},
		Input: &cos.JobInput{
			Object: "video/" + name + ".mp4",
		},
	})
	l.Info("Create job resp", resp)
}

func checkConvertTemplate() string {
	result, _, err := Cos().CI.DescribeMediaTemplate(context.Background(), &cos.DescribeMediaTemplateOptions{
		Tag:  "Transcode",
		Name: "video-converter",
	})

	if err != nil || result.TotalCount == 0 {
		return createConvertTemplate()
	}
	l.Info("response.getTemplateList() size is {}", len(result.TemplateList))
	return result.TemplateList[0].TemplateId
}

func createConvertTemplate() string {
	result, _, _ := Cos().CI.CreateMediaTranscodeTemplate(context.Background(), &cos.CreateMediaTranscodeTemplateOptions{
		Tag:  "Transcode",
		Name: "video-converter",
		Container: &cos.Container{
			Format: "mp4",
		},
		Video: &cos.Video{
			Codec:   "H.264",
			Bitrate: "4800",
			Pixfmt:  "yuv420p",
			Profile: "high",
		},
		Audio: &cos.Audio{
			Codec:      "aac",
			Samplerate: "44100",
		},
	})
	return result.Template.TemplateId
}

func getVideoMetadata(key string, video *model.TPhoto) {
	result, _, err := Cos().CI.GetMediaInfo(context.Background(), key, nil)
	if err != nil {
		l.Error("Can't get Media info", err)
	} else {
		videoInfo := result.MediaInfo.Stream.Video[0]
		video.Width = parseInt(videoInfo.Width)
		video.Height = parseInt(videoInfo.Height)
		video.Duration = parseFloat(result.MediaInfo.Format.Duration)
	}
}

func generatePoster(key string, video *model.TPhoto) {
	opt := &cos.PostSnapshotOptions{
		Input: &cos.JobInput{
			Object: key,
		},
		Output: &cos.JobOutput{
			Bucket: viper.GetString("cos.bucket"),
			Region: viper.GetString("cos.region"),
			Object: "small/" + video.Name,
		},
		Time: "1",
		Mode: "keyframe",
	}
	Cos().CI.PostSnapshot(context.Background(), opt)
}

func checkInBlacklist(p *model.TPhoto) {
	var existBlock model.TBlockPhoto
	db().Where("md5", p.MD5).Where("sha1", p.SHA1).Where("size", p.Size).First(&existBlock)
	if existBlock.ID > 0 {
		l.Info("在黑名单里面，转移到回收站！")
		p.Trashed = true
		db().Delete(&existBlock)
	}
}

func sameFileExist(file *os.File, key, name string, size int64) *model.TPhoto {
	md5 := getFileMD5(file)
	sha1 := getFileSha1(file)
	var exist model.TPhoto
	db().Where("md5", md5).Where("sha1", sha1).Where("size", size).First(&exist)
	if exist.ID > 0 {
		l.Info(fmt.Sprintf("与图片 %d 重复，file %s 被抛弃！", exist.ID, key))
		if !strings.Contains(exist.Description, name) {
			exist.Description += "\n" + name
			db().Save(exist)
		}
		Cos().Object.Delete(context.Background(), key)
		return &exist
	} else {
		return &model.TPhoto{
			Name:        uuid.New().String(),
			Size:        size,
			MD5:         md5,
			SHA1:        sha1,
			Description: name,
		}
	}
}
