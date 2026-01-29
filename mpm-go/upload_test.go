package main

import (
	"os"
	"testing"

	"github.com/evanoberholster/imagemeta"
	"github.com/magiconair/properties/assert"
)

func Test_FullPath(t *testing.T) {
	s := `form-data; name="file"; filename="tmp/icon_ne4.png"`
	assert.Equal(t, "tmp/icon_ne4.png", getFullPathName(s))
}

func Test_UploadFile(t *testing.T) {
	uploadFileToCos("upload/1736081374298_tmp/icon04.png", "image/png", 55908, "/tmp/mpm3038335038icon04.png")
	uploadFile("upload/1736081374298_tmp/icon04.png", "1656238974000", "image/png", 55908, "/tmp/mpm3038335038icon04.png", "testuser")
}

func Test_UploadVideo(t *testing.T) {
	uploadFileToCos("upload/1736087278894_tmp/VID_A1C1_WhoresairPowerup_Master_Opt.mp4", "video/mpeg4", 12664019, "/tmp/mpm293823983VID_A1C1_WhoresairPowerup_Master_Opt.mp4")
	uploadFile("upload/1736087278894_tmp/VID_A1C1_WhoresairPowerup_Master_Opt.mp4", "1656238974000", "video/mpeg4", 12664019, "/tmp/mpm293823983VID_A1C1_WhoresairPowerup_Master_Opt.mp4", "testuser")
}

func Test_FileExif(t *testing.T) {
	file, err := os.Open("/tmp/mpm2878391466icon_ne4.png")
	t.Log(err)
	exif, err := imagemeta.Decode(file)
	t.Log(err)
	t.Log(exif)
}

func Test_FFProbe(t *testing.T) {
	// 测试 ffprobe 获取视频元数据
	// 注意: 需要有一个实际的视频文件进行测试
	videoPath := "/tmp/test_video.mp4"
	if _, err := os.Stat(videoPath); os.IsNotExist(err) {
		t.Skip("Test video file not found, skipping ffprobe test")
		return
	}
	
	info, err := getVideoMetadataFromFFProbe(videoPath)
	if err != nil {
		t.Fatal("Failed to get video metadata:", err)
	}
	
	t.Log("Video metadata:", info)
	
	// 检查是否获取到了视频流信息
	hasVideoStream := false
	for _, stream := range info.Streams {
		if stream.CodecType == "video" {
			hasVideoStream = true
			t.Logf("Video stream found - Width: %d, Height: %d", stream.Width, stream.Height)
		}
	}
	
	if !hasVideoStream {
		t.Error("No video stream found")
	}
	
	// 检查时长信息
	if info.Format.Duration != "" {
		t.Log("Duration:", info.Format.Duration)
	}
	
	// 尝试提取创建时间
	if creationTime, err := extractVideoCreationTime(info); err == nil {
		t.Log("Creation time:", creationTime)
	} else {
		t.Log("No creation time found:", err)
	}
}

func Test_FFmpegThumbnail(t *testing.T) {
	// 测试使用ffmpeg生成视频缩略图
	videoPath := "/tmp/test_video.mp4"
	if _, err := os.Stat(videoPath); os.IsNotExist(err) {
		t.Skip("Test video file not found, skipping ffmpeg thumbnail test")
		return
	}
	
	outputPath := "/tmp/test_thumbnail.jpg"
	defer os.Remove(outputPath)
	
	err := generateVideoThumbnailWithFFmpeg(videoPath, outputPath)
	if err != nil {
		t.Fatal("Failed to generate thumbnail:", err)
	}
	
	// 检查缩略图是否生成成功
	if _, err := os.Stat(outputPath); os.IsNotExist(err) {
		t.Error("Thumbnail file not created")
	} else {
		t.Log("Thumbnail generated successfully at:", outputPath)
	}
}
