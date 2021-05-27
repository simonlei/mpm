package org.mpm.server.pics;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoRequest;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoResponse;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoVideo;
import com.qcloud.cos.model.ciModel.snapshot.SnapshotRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityBlockPicture;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.mapl.Mapl;
import org.nutz.trans.Trans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PicsModule {

    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    @Autowired
    Dao dao;
    @Autowired
    COSClient cosClient;

    @Value("${qqlbsKey}")
    String qqlbsKey;
    @Value("${qqlbsToken}")
    String qqlbsToken;
    @Value("${cos.bucket}")
    String bucket;

    public int count(boolean trashed) {
        return dao.count(EntityPhoto.class, Cnd.where("trashed", "=", trashed));
    }

    public EntityPhoto savePhotoInDb(EntityFile parent, String key, String name) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(name, "" + Math.random());
            String contentType = saveCosFile(key, tmpFile);
            if (contentType.contains("video")) {
                return saveVideo(tmpFile, key, Files.getMajorName(name));
            }
            return saveFileInRepository(tmpFile, key, Files.getMajorName(name));
        } catch (Exception e) {
            log.error("Can't save file " + key, e);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }

        return null;
    }

    public String saveCosFile(String key, File tmpFile) throws FileNotFoundException {
        COSObject cosObj = cosClient.getObject(bucket, key);
        String contentType = cosObj.getObjectMetadata().getContentType();
        Streams.writeAndClose(new FileOutputStream(tmpFile), cosObj.getObjectContent());
        return contentType;
    }

    private EntityPhoto saveVideo(File file, String key, String name) {
        // 去重
        EntityPhoto samePhoto = sameFileExist(file, key);
        if (samePhoto != null) {
            return samePhoto;
        }
        EntityPhoto video = createEntityPhotoFrom(file);
        video.setMediaType(VIDEO);
        checkInBlacklist(file, video);
        log.info("Saving video " + video.getId());
        video = dao.insert(video, true, false, false);

        generatePoster(key, video);
        getVideoMetadata(key, video);
        // TODO: 应该有办法获取到video的拍摄时间吧？
        video.setTakenDate(new Date());
        dao.updateIgnoreNull(video);

        cosClient.copyObject(bucket, key, bucket, "video/" + video.getName());
        cosClient.deleteObject(bucket, key);

        return video;
    }

    private void getVideoMetadata(String key, EntityPhoto video) {
        try {
            // get metadata
            MediaInfoRequest request = new MediaInfoRequest();
            request.setBucketName(bucket);
            request.getInput().setObject(key);
            MediaInfoResponse response = cosClient.generateMediainfo(request);
            MediaInfoVideo infoVideo = response.getMediaInfo().getStream().getVideo();
            String duration = infoVideo.getDuration();
            video.setWidth(Integer.parseInt(infoVideo.getWidth()));
            video.setHeight(Integer.parseInt(infoVideo.getHeight()));
            video.setDuration(Double.parseDouble(duration));
        } catch (Exception e) {
            log.error("Can't get metadata of " + key + " id " + video.getId(), e);
        }
    }

    private void generatePoster(String key, EntityPhoto video) {
        try {
            // generate poster
            SnapshotRequest snapshotRequest = new SnapshotRequest();
            snapshotRequest.setBucketName(bucket);
            snapshotRequest.getInput().setObject(key);
            snapshotRequest.getOutput().setBucket(bucket);
            snapshotRequest.getOutput()
                    .setRegion(cosClient.getClientConfig().getRegion().getRegionName());
            snapshotRequest.getOutput().setObject(video.getName());
            snapshotRequest.setMode("keyframe");
            snapshotRequest.setTime("1");
            cosClient.generateSnapshot(snapshotRequest);
        } catch (Exception e) {
            log.error("Can't generate poster " + key + " id " + video.getId(), e);
        }
    }

    public EntityPhoto saveFileInRepository(File file, String key, String name) {
        try {
            final BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null; // 不是正确的图片
            }

            EntityPhoto samePhoto = sameFileExist(file, key);
            if (samePhoto != null) {
                return samePhoto;
            }
            EntityPhoto photo = createEntityPhotoFrom(file);

            photo.setWidth(image.getWidth());
            photo.setHeight(image.getHeight());
            setDateFromExif(file, photo);
            checkInBlacklist(file, photo);
            log.info("Saving photo " + photo.getId());
            photo = dao.insert(photo, true, false, false);

            log.info("Photo:" + photo);
            cosClient.copyObject(bucket, key, bucket, photo.getName());
            cosClient.deleteObject(bucket, key);
            return photo;
        } catch (IOException e) {
            log.error("Can't read file " + key, e);
            return null;
        }
    }

    private void checkInBlacklist(File file, EntityPhoto photo) {
        EntityBlockPicture blackPhoto = inBlackList(file);
        if (blackPhoto != null) {
            log.info("在黑名单里面，转移到回收站！");
            photo.setTrashed(true);
            dao.delete(blackPhoto);
        }
    }

    private EntityPhoto createEntityPhotoFrom(File file) {
        EntityPhoto photo = new EntityPhoto();
        photo.setSize(file.length());

        photo.setMd5(Lang.md5(file));
        photo.setSha1(Lang.sha1(file));
        return photo;
    }

    private EntityPhoto sameFileExist(File file, String key) {
        String md5 = Lang.md5(file);
        String sha1 = Lang.sha1(file);
        EntityPhoto existPhoto = dao.fetch(EntityPhoto.class,
                Cnd.where("md5", "=", md5).and("sha1", "=", sha1).and("size", "=", file.length()));
        if (existPhoto != null) {
            log.info("与图片 " + existPhoto.getId() + " 重复，file " + key + " 被抛弃！");
            cosClient.deleteObject(bucket, key);
        }
        return existPhoto;
    }

    private EntityBlockPicture inBlackList(File file) {
        String md5 = Lang.md5(file);
        String sha1 = Lang.sha1(file);
        EntityBlockPicture existPhoto = dao.fetch(EntityBlockPicture.class,
                Cnd.where("md5", "=", md5).and("sha1", "=", sha1).and("size", "=", file.length()));

        return existPhoto;
    }

    public void setDateFromExif(File file, EntityPhoto photo) {
        if (photo.getTakenDate() == null) { // 如果已经有就不要重复设置了
            photo.setTakenDate(new Date(file.lastModified()));
        }
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(file);
            setDate(photo, metadata);
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                photo.setLatitude(gpsDirectory.getGeoLocation().getLatitude());
                photo.setLongitude(gpsDirectory.getGeoLocation().getLongitude());

                photo.setAddress(getAddress(photo.getLatitude(), photo.getLongitude()));
            }
        } catch (Throwable e) {
            log.error("Can't read exif info", e);
        }
    }

    private void setDate(EntityPhoto photo, Metadata metadata) {
        try {
            ExifSubIFDDirectory directory =
                    metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null && directory.getDateOriginal() != null) {
                photo.setTakenDate(directory.getDateOriginal());
            }
        } catch (Exception e) {
            log.error("Can't read exif date", e);
        }
    }

    public String getAddress(double latitude, double longtitude) {
        try {
            String requestStr = "key=" + qqlbsKey + "&location=" + latitude + "," + longtitude;

            String sig = Lang.md5("/ws/geocoder/v1?" + requestStr + qqlbsToken);

            String url = "https://apis.map.qq.com/ws/geocoder/v1?" + requestStr + "&sig=" + sig;
            log.info("qqlbs url is " + url);
            Response response = Http.get(url);
            String result = response.getContent();
            log.info("qqlbs result:" + result);
            return (String) Mapl.cell(Json.fromJson(result), "result.address");
        } catch (Exception e) {
            log.error("Can't get lbs address", e);
            return "";
        }
    }

    public void realDelete(EntityPhoto photo) {
        final EntityBlockPicture blackList = new EntityBlockPicture();
        blackList.setMd5(photo.getMd5());
        blackList.setSha1(photo.getSha1());
        blackList.setSize(photo.getSize());

        Trans.exec(() -> {
            try {
                cosClient.deleteObject(bucket, photo.getName());
                dao.insert(blackList);
                dao.delete(photo);
                dao.clear(EntityFile.class, Cnd.where("photoId", "=", photo.getId()));
            } catch (Throwable e) {
                log.error("Can't real delete photo.", e);
            }
        });
    }
}
