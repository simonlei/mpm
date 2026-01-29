package main

import (
	"context"
	"encoding/json"
	"fmt"
	"image"
	_ "image/gif"
	_ "image/jpeg"
	_ "image/png"
	"mpm-go/model"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/disintegration/imaging"
	"github.com/evanoberholster/imagemeta"
	"github.com/google/uuid"
	"github.com/spf13/viper"
	"github.com/tencentyun/cos-go-sdk-v5"
	"github.com/tidwall/gjson"
	"gorm.io/gorm"
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

func savePhotoInDb(key, name, lastModified, contentType string, size int64, fileName string) *model.TPhoto {
	if strings.Contains(contentType, "video") {
		return saveVideo(fileName, size, lastModified, key, getMajorName(name))
	} else {
		return saveImage(fileName, size, lastModified, key, getMajorName(name))
	}
}

func saveImage(fileName string, size int64, lastModified, key, name string) *model.TPhoto {
	photo := sameFileExist(fileName, key, name, size)
	if photo.ID > 0 {
		return photo
	}
	photo.MediaType = "photo"
	photo.Description = name
	setInfosFromCos(key, photo)
	setInfosFromFile(fileName, lastModified, photo)
	checkInBlacklist(photo)
	db().Create(photo)
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

func realDelete(p *model.TPhoto) {
	bl := model.TBlockPhoto{
		MD5:  p.MD5,
		SHA1: p.SHA1,
		Size: p.Size,
	}
	db().Transaction(func(tx *gorm.DB) error {
		Cos().Object.Delete(context.Background(), "small/"+p.Name)
		if p.MediaType == "video" {
			Cos().Object.Delete(context.Background(), "video/"+p.Name+".mp4")
			Cos().Object.Delete(context.Background(), "video_t/"+p.Name+".mp4")
		} else {
			Cos().Object.Delete(context.Background(), "origin/"+p.Name)
		}
		tx.Create(&bl)
		tx.Delete(&p)
		tx.Delete(&model.TFile{}, "photo_id = ?", p.ID)
		tx.Delete(&model.PhotoFaceInfo{}, "photo_id =?", p.ID)
		return nil
	})
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
		// 如果COS图片处理失败（比如图片太大），尝试本地生成缩略图
		l.Warn("COS can't generate small pic, trying local generation", err)
		if localErr := generateSmallPicLocally(key, name); localErr != nil {
			l.Error("Can't generate small pic locally either", localErr)
		}
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

// generateSmallPicLocally 在本地生成缩略图并上传到COS
func generateSmallPicLocally(key, name string) error {
	// 1. 从COS下载原图到临时文件
	tmpDir := os.TempDir()
	tmpOriginal := filepath.Join(tmpDir, fmt.Sprintf("original_%s", uuid.New().String()))
	
	// 根据配置决定输出格式和扩展名
	format := viper.GetString("smallphoto.format")
	var ext string
	switch format {
	case "webp", "png":
		ext = ".jpg" // webp不支持，统一用jpg
	default:
		ext = ".jpg"
	}
	tmpSmall := filepath.Join(tmpDir, fmt.Sprintf("small_%s%s", uuid.New().String(), ext))
	
	defer func() {
		os.Remove(tmpOriginal)
		os.Remove(tmpSmall)
	}()

	// 下载原图
	resp, err := Cos().Object.Get(context.Background(), key, nil)
	if err != nil {
		return fmt.Errorf("failed to download original image: %w", err)
	}
	defer resp.Body.Close()

	// 保存到临时文件
	tmpFile, err := os.Create(tmpOriginal)
	if err != nil {
		return fmt.Errorf("failed to create temp file: %w", err)
	}
	defer tmpFile.Close()

	if _, err = tmpFile.ReadFrom(resp.Body); err != nil {
		return fmt.Errorf("failed to save temp file: %w", err)
	}
	tmpFile.Close()

	// 2. 打开并解码图片
	srcImage, err := imaging.Open(tmpOriginal)
	if err != nil {
		return fmt.Errorf("failed to open image: %w", err)
	}

	// 3. 生成缩略图 (最大尺寸 2560x1440)
	bounds := srcImage.Bounds()
	width := bounds.Dx()
	height := bounds.Dy()
	
	maxWidth := 2560
	maxHeight := 1440
	
	// 计算缩放比例，保持宽高比
	var thumbnail image.Image
	if width > maxWidth || height > maxHeight {
		thumbnail = imaging.Fit(srcImage, maxWidth, maxHeight, imaging.Lanczos)
	} else {
		thumbnail = srcImage
	}

	// 4. 保存缩略图，使用JPEG格式（通用且高效）
	saveErr := imaging.Save(thumbnail, tmpSmall, imaging.JPEGQuality(85))
	if saveErr != nil {
		return fmt.Errorf("failed to save thumbnail: %w", saveErr)
	}

	// 5. 上传缩略图到COS的 /small/ 路径
	smallFile, err := os.Open(tmpSmall)
	if err != nil {
		return fmt.Errorf("failed to open thumbnail: %w", err)
	}
	defer smallFile.Close()

	_, err = Cos().Object.Put(context.Background(), "small/"+name, smallFile, nil)
	if err != nil {
		return fmt.Errorf("failed to upload thumbnail to COS: %w", err)
	}

	l.Info("Successfully generated and uploaded thumbnail locally for", name)
	return nil
}

func setInfosFromFile(fileName string, lastModified string, photo *model.TPhoto) {
	if time.Time(photo.TakenDate).IsZero() {
		photo.TakenDate = model.DateTime(time.UnixMilli(parseInt64(lastModified)))
	}
	if file, err := os.Open(fileName); err != nil {
		l.Error("Can't open file")
	} else {
		defer file.Close()
		
		// 如果宽高为0，尝试从图片文件获取尺寸信息
		if photo.Width == 0 || photo.Height == 0 {
			if imgConfig, _, err := image.DecodeConfig(file); err == nil {
				photo.Width = imgConfig.Width
				photo.Height = imgConfig.Height
			}
			// 重置文件指针到开头，以便后续读取exif
			file.Seek(0, 0)
		}
		
		exif, err := imagemeta.Decode(file)
		if err != nil {
			l.Error("Can't read exif info", err)
		} else {
			if !exif.DateTimeOriginal().IsZero() {
				photo.TakenDate = model.DateTime(exif.DateTimeOriginal())
			}
			if exif.GPS.Latitude() > 0 && exif.GPS.Longitude() > 0 {
				photo.Latitude = exif.GPS.Latitude()
				photo.Longitude = exif.GPS.Longitude()
				photo.Address = getGisAddress(photo.Latitude, photo.Longitude)
			}
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
	resp, err := Cos().CI.Get(context.Background(), key, "imageInfo", nil)
	if err != nil {
		l.Error("Can't get image info", err)
	} else {
		var info ImageInfo
		json.Unmarshal(convertToBytes(resp.Body), &info)
		photo.Width = parseInt(info.Width)
		photo.Height = parseInt(info.Height)
	}
	resp, err = Cos().CI.Get(context.Background(), key, "exif", nil)
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

func saveVideo(fileName string, size int64, lastModified, key, name string) *model.TPhoto {
	video := sameFileExist(fileName, key, name, size)
	if video.ID > 0 {
		return video
	}
	video.MediaType = "video"
	checkInBlacklist(video)
	db().Create(&video)
	
	// 使用 ffprobe 获取视频元数据
	ffprobeInfo, err := getVideoMetadataFromFFProbe(fileName)
	if err != nil {
		l.Warn("Failed to get video metadata from ffprobe:", err)
	} else {
		// 提取视频创建时间
		if creationTime, err := extractVideoCreationTime(ffprobeInfo); err == nil {
			video.TakenDate = model.DateTime(creationTime)
			l.Info("Got video creation time from ffprobe:", creationTime)
		} else {
			// 如果无法从 ffprobe 获取创建时间，使用 lastModified
			video.TakenDate = model.DateTime(time.UnixMilli(parseInt64(lastModified)))
			l.Info("Using lastModified as creation time:", lastModified)
		}
		
		// 从 ffprobe 获取宽高和时长信息
		for _, stream := range ffprobeInfo.Streams {
			if stream.CodecType == "video" {
				video.Width = stream.Width
				video.Height = stream.Height
				break
			}
		}
		if ffprobeInfo.Format.Duration != "" {
			video.Duration = parseFloat(ffprobeInfo.Format.Duration)
		}
	}
	
	generatePoster(key, video)
	
	// 从 COS CI API 获取视频元数据
	getVideoMetadata(key, video)
	
	// 如果 COS CI API 返回的宽高都是 0，使用 ffprobe 的数据
	if (video.Width == 0 || video.Height == 0) && ffprobeInfo != nil {
		for _, stream := range ffprobeInfo.Streams {
			if stream.CodecType == "video" && stream.Width > 0 && stream.Height > 0 {
				video.Width = stream.Width
				video.Height = stream.Height
				l.Info("Using ffprobe dimensions:", video.Width, "x", video.Height)
				break
			}
		}
		// 如果时长也是0，使用 ffprobe 的时长
		if video.Duration == 0 && ffprobeInfo.Format.Duration != "" {
			video.Duration = parseFloat(ffprobeInfo.Format.Duration)
			l.Info("Using ffprobe duration:", video.Duration)
		}
	}
	
	// 如果创建时间仍然为零，使用 lastModified
	if time.Time(video.TakenDate).IsZero() {
		video.TakenDate = model.DateTime(time.UnixMilli(parseInt64(lastModified)))
	}
	
	db().Save(&video)
	_, _, err = Cos().Object.Copy(context.Background(), "video/"+video.Name+".mp4",
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
	result, _, err := Cos().CI.CreateMediaTranscodeTemplate(context.Background(), &cos.CreateMediaTranscodeTemplateOptions{
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
	if err != nil {
		l.Error(err)
	}
	return result.Template.TemplateId
}

// ffprobeVideoInfo 使用 ffprobe 获取视频元数据的结构体
type ffprobeVideoInfo struct {
	Streams []struct {
		CodecType string `json:"codec_type"`
		Width     int    `json:"width"`
		Height    int    `json:"height"`
	} `json:"streams"`
	Format struct {
		Duration string            `json:"duration"`
		Tags     map[string]string `json:"tags"`
	} `json:"format"`
}

// getVideoMetadataFromFFProbe 使用 ffprobe 从本地视频文件获取元数据
func getVideoMetadataFromFFProbe(filePath string) (*ffprobeVideoInfo, error) {
	cmd := exec.Command("ffprobe",
		"-v", "quiet",
		"-print_format", "json",
		"-show_format",
		"-show_streams",
		filePath)
	
	output, err := cmd.Output()
	if err != nil {
		return nil, fmt.Errorf("ffprobe execution failed: %w", err)
	}
	
	var info ffprobeVideoInfo
	if err := json.Unmarshal(output, &info); err != nil {
		return nil, fmt.Errorf("failed to parse ffprobe output: %w", err)
	}
	
	return &info, nil
}

// extractVideoCreationTime 从 ffprobe 信息中提取视频创建时间
func extractVideoCreationTime(info *ffprobeVideoInfo) (time.Time, error) {
	// 尝试从不同的标签字段中获取创建时间
	tags := info.Format.Tags
	
	// 常见的创建时间标签
	timeKeys := []string{"creation_time", "date", "com.apple.quicktime.creationdate"}
	
	for _, key := range timeKeys {
		if timeStr, ok := tags[key]; ok && timeStr != "" {
			// 尝试多种时间格式解析
			formats := []string{
				time.RFC3339,
				"2006-01-02T15:04:05.000000Z",
				"2006-01-02 15:04:05",
				"2006-01-02T15:04:05",
			}
			
			for _, format := range formats {
				if t, err := time.Parse(format, timeStr); err == nil {
					return t, nil
				}
			}
		}
	}
	
	return time.Time{}, fmt.Errorf("no valid creation time found")
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
	_, _, err := Cos().CI.PostSnapshot(context.Background(), opt)
	if err != nil {
		l.Error("Can't generate poster", err)
	}
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

func sameFileExist(fileName string, key, name string, size int64) *model.TPhoto {
	md5 := getFileMD5(fileName)
	sha1 := getFileSha1(fileName)
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

// fixPhotoWithZeroDimension 修复单张宽高为0的图片或视频
func fixPhotoWithZeroDimension(photo *model.TPhoto) error {
	l.Info("Fixing photo/video", photo.ID, photo.Name, "media_type:", photo.MediaType)
	
	// 1. 从COS下载原图/视频到临时文件
	tmpDir := os.TempDir()
	tmpOriginal := filepath.Join(tmpDir, fmt.Sprintf("fix_%s", uuid.New().String()))
	tmpSmall := filepath.Join(tmpDir, fmt.Sprintf("fix_small_%s.jpg", uuid.New().String()))
	
	defer func() {
		os.Remove(tmpOriginal)
		os.Remove(tmpSmall)
	}()

	// 确定原图在COS中的路径
	var cosKey string
	if photo.MediaType == "video" {
		cosKey = "video/" + photo.Name + ".mp4"
	} else {
		cosKey = "origin/" + photo.Name
	}

	// 下载原图/视频
	resp, err := Cos().Object.Get(context.Background(), cosKey, nil)
	if err != nil {
		return fmt.Errorf("failed to download original file: %w", err)
	}
	defer resp.Body.Close()

	// 保存到临时文件
	tmpFile, err := os.Create(tmpOriginal)
	if err != nil {
		return fmt.Errorf("failed to create temp file: %w", err)
	}
	defer tmpFile.Close()

	if _, err = tmpFile.ReadFrom(resp.Body); err != nil {
		return fmt.Errorf("failed to save temp file: %w", err)
	}
	tmpFile.Close()

	// 2. 根据媒体类型处理
	if photo.MediaType == "video" {
		// 使用ffprobe获取视频元数据
		ffprobeInfo, err := getVideoMetadataFromFFProbe(tmpOriginal)
		if err != nil {
			return fmt.Errorf("failed to get video metadata from ffprobe: %w", err)
		}

		// 更新宽高
		for _, stream := range ffprobeInfo.Streams {
			if stream.CodecType == "video" && stream.Width > 0 && stream.Height > 0 {
				photo.Width = stream.Width
				photo.Height = stream.Height
				l.Info("Updated video dimensions from ffprobe", photo.ID, "width:", photo.Width, "height:", photo.Height)
				break
			}
		}

		// 更新时长
		if ffprobeInfo.Format.Duration != "" {
			photo.Duration = parseFloat(ffprobeInfo.Format.Duration)
			l.Info("Updated video duration from ffprobe", photo.ID, "duration:", photo.Duration)
		}

		// 尝试提取创建时间
		if creationTime, err := extractVideoCreationTime(ffprobeInfo); err == nil {
			if time.Time(photo.TakenDate).IsZero() || time.Time(photo.TakenDate).Year() < 2000 {
				photo.TakenDate = model.DateTime(creationTime)
				l.Info("Updated video creation time from ffprobe", photo.ID, "taken_date:", creationTime)
			}
		}

		// 保存视频元数据到数据库
		if err := db().Save(photo).Error; err != nil {
			return fmt.Errorf("failed to update video metadata: %w", err)
		}

		l.Info("Successfully fixed video metadata", photo.ID, photo.Name)
	} else if photo.MediaType == "photo" {
		// 打开图片获取尺寸
		srcImage, err := imaging.Open(tmpOriginal)
		if err != nil {
			return fmt.Errorf("failed to open image: %w", err)
		}

		bounds := srcImage.Bounds()
		width := bounds.Dx()
		height := bounds.Dy()

		// 更新数据库中的宽高
		photo.Width = width
		photo.Height = height
		if err := db().Save(photo).Error; err != nil {
			return fmt.Errorf("failed to update photo dimensions: %w", err)
		}

		l.Info("Updated photo dimensions", photo.ID, "width:", width, "height:", height)

		// 生成缩略图
		maxWidth := 2560
		maxHeight := 1440
		var thumbnail image.Image
		if width > maxWidth || height > maxHeight {
			thumbnail = imaging.Fit(srcImage, maxWidth, maxHeight, imaging.Lanczos)
		} else {
			thumbnail = srcImage
		}

		// 保存缩略图
		if err := imaging.Save(thumbnail, tmpSmall, imaging.JPEGQuality(85)); err != nil {
			return fmt.Errorf("failed to save thumbnail: %w", err)
		}

		// 上传缩略图到COS
		smallFile, err := os.Open(tmpSmall)
		if err != nil {
			return fmt.Errorf("failed to open thumbnail: %w", err)
		}
		defer smallFile.Close()

		_, err = Cos().Object.Put(context.Background(), "small/"+photo.Name, smallFile, nil)
		if err != nil {
			return fmt.Errorf("failed to upload thumbnail to COS: %w", err)
		}

		l.Info("Successfully fixed photo and uploaded thumbnail", photo.ID, photo.Name)
	}

	return nil
}

// fixPhotosWithZeroDimensions 批量修复宽高为0的图片和视频
func fixPhotosWithZeroDimensions() (int, int, error) {
	var items []model.TPhoto
	
	// 查找宽高为0的图片和视频
	if err := db().Where("(width = 0 OR height = 0)").
		Where("trashed = ?", false).
		Find(&items).Error; err != nil {
		return 0, 0, fmt.Errorf("failed to query photos/videos: %w", err)
	}

	total := len(items)
	success := 0
	
	l.Info("Found photos/videos with zero dimensions:", total)

	for _, item := range items {
		if err := fixPhotoWithZeroDimension(&item); err != nil {
			l.Error("Failed to fix photo/video", item.ID, item.MediaType, err)
		} else {
			success++
		}
	}

	return total, success, nil
}
