package org.mpm.server.pics;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoRequest;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoResponse;
import com.qcloud.cos.model.ciModel.mediaInfo.MediaInfoVideo;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.model.ciModel.persistence.PicOperations.Rule;
import com.qcloud.cos.model.ciModel.snapshot.SnapshotRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityBlockPicture;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityMeta;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.remote.CosRemoteService;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.el.El;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.trans.Trans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PicsService {

    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    final Dao dao;
    final COSClient cosClient;
    final CosRemoteService cosRemoteService;
    @Autowired
    GisService gisService;

    @Value("${cos.bucket}")
    String bucket;

    public PicsService(Dao dao, COSClient cosClient, CosRemoteService cosRemoteService) {
        this.dao = dao;
        this.cosClient = cosClient;
        this.cosRemoteService = cosRemoteService;
    }

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
            } else {
                return saveImage(tmpFile, key, Files.getMajorName(name));
            }
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

        CopyObjectRequest request = new CopyObjectRequest(bucket, key, bucket, "video/" + video.getName() + ".mp4");
        // request.setStorageClass(StorageClass.Archive);
        cosClient.copyObject(request);
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
            snapshotRequest.getOutput().setObject("small/" + video.getName());
            snapshotRequest.setMode("keyframe");
            snapshotRequest.setTime("1");
            cosClient.generateSnapshot(snapshotRequest);
        } catch (Exception e) {
            log.error("Can't generate poster " + key + " id " + video.getId(), e);
        }
    }

    public EntityPhoto saveImage(File file, String key, String name) {
        EntityPhoto samePhoto = sameFileExist(file, key);
        if (samePhoto != null) {
            return samePhoto;
        }
        EntityPhoto photo = createEntityPhotoFrom(file);

        setInfosFromCos(key, photo);
        setInfosFromFile(file, photo);
        checkInBlacklist(file, photo);
        log.info("Saving photo " + photo.getId());
        photo = dao.insert(photo, true, false, false);

        log.info("Photo:" + photo);
        savePhotosOnCos(key, photo);
        return photo;
    }

    private void savePhotosOnCos(String key, EntityPhoto photo) {
        CopyObjectRequest request = new CopyObjectRequest(bucket, key, bucket, "/origin/" + photo.getName());
        // 归档太麻烦，还要先恢复才行
        // request.setStorageClass(StorageClass.Archive);
        cosClient.copyObject(request);
        generateSmallPic(key, photo.getName());
        cosClient.deleteObject(bucket, key);
    }

    private void generateSmallPic(String key, String name) {
        ImageProcessRequest request = new ImageProcessRequest(bucket, key);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        List<Rule> ruleList = new ArrayList<>();
        PicOperations.Rule rule1 = new PicOperations.Rule();
        rule1.setBucket(bucket);
        rule1.setFileId("/small/" + name);
        rule1.setRule("imageMogr2/thumbnail/2560x1440");
        ruleList.add(rule1);
        picOperations.setRules(ruleList);
        request.setPicOperations(picOperations);

        CIUploadResult result = cosClient.processImage(request);
        String format = result.getProcessResults().getObjectList().get(0).getFormat();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + format);
        cosClient.updateObjectMetaData(bucket, "small/" + name, metadata);
        log.info("ci upload result: " + Json.toJson(result));
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

    void setInfosFromCos(String key, EntityPhoto photo) {
        Map imageInfo = cosRemoteService.getImageInfo(key);
        photo.setWidth(MyUtils.parseInt(imageInfo.get("width"), 0));
        photo.setHeight(MyUtils.parseInt(imageInfo.get("height"), 0));

        Map exifInfo = cosRemoteService.getExifInfo(key);
        if (exifInfo.get("error") != null) {
            return;
        }
        String dateTime = MyUtils.cell(exifInfo, "DateTime.val");
        Date date = MyUtils.parseDate(dateTime, "yyyy:MM:dd HH:mm:ss");
        photo.setTakenDate(date);
        photo.setLatitude(parseGps(MyUtils.cell(exifInfo, "GPSLatitude.val")));
        photo.setLongitude(parseGps(MyUtils.cell(exifInfo, "GPSLongitude.val")));
    }

    Double parseGps(String str) {
        try {
            String[] strs = str.split(" ");
            return (Double) El.eval("1.0*" + strs[0]) + (Double) El.eval("1.0*" + strs[1]) / 60
                    + (Double) El.eval("1.0*" + strs[2]) / 3600;
        } catch (Exception e) {
            return null;
        }
    }

    public void setInfosFromFile(File file, EntityPhoto photo) {
        if (photo.getTakenDate() == null) { // 如果已经有就不要重复设置了
            photo.setTakenDate(new Date(file.lastModified()));
        }
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            setDate(photo, metadata);
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                photo.setLatitude(gpsDirectory.getGeoLocation().getLatitude());
                photo.setLongitude(gpsDirectory.getGeoLocation().getLongitude());

                photo.setAddress(gisService.getAddress(photo.getLatitude(), photo.getLongitude()));
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

    public void realDelete(EntityPhoto photo) {
        final EntityBlockPicture blackList = new EntityBlockPicture();
        blackList.setMd5(photo.getMd5());
        blackList.setSha1(photo.getSha1());
        blackList.setSize(photo.getSize());

        Trans.exec(() -> {
            try {
                cosClient.deleteObject(bucket, "small/" + photo.getName());
                if (Lang.equals("video", photo.getMediaType())) {
                    cosClient.deleteObject(bucket, "video/" + photo.getName() + ".mp4");
                    cosClient.deleteObject(bucket, "video_t/" + photo.getName() + ".mp4");
                } else {
                    cosClient.deleteObject(bucket, "origin/" + photo.getName());
                }
                dao.insert(blackList);
                dao.delete(photo);
                dao.clear(EntityFile.class, Cnd.where("photoId", "=", photo.getId()));
            } catch (Throwable e) {
                log.error("Can't real delete photo.", e);
            }
        });
    }

    @Deprecated
    public void checkSmallPhotos() {
        EntityMeta meta = dao.fetch(EntityMeta.class, "lastSmallPhotoCheckId");
        long lastId = meta == null ? 0 : Long.parseLong(meta.getValue());
        // get first 20
        List<EntityPhoto> photos = dao.query(EntityPhoto.class, Cnd.where("id", ">", lastId)
                .orderBy("id", "asc"), new Pager(1, 20));
        for (EntityPhoto p : photos) {
            try {
                if (VIDEO.equals(p.getMediaType())) {
                    cosClient.copyObject(bucket, p.getName(), bucket, "small/" + p.getName());
                    cosClient.deleteObject(bucket, p.getName());
                } else {
                    savePhotosOnCos(p.getName(), p);
                }
            } catch (Exception e) {
                log.error("Can't check photo:" + p.getId(), e);
            }
            lastId = p.getId();
        }
        if (meta == null) {
            meta = EntityMeta.builder().key("lastSmallPhotoCheckId").build();
        }
        meta.setValue("" + lastId);
        dao.insertOrUpdate(meta);
    }
}
