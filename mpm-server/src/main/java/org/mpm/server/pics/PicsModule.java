package org.mpm.server.pics;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityBlockPicture;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.util.ExplicitPager;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.mapl.Mapl;
import org.nutz.trans.Trans;

@IocBean(create = "init")
@Slf4j
public class PicsModule {

    @Inject
    Dao dao;
    @Inject
    COSClient cosClient;
    @Inject
    PropertiesProxy conf;

    String qqlbsKey;
    String qqlbsToken;
    String bucket;

    public int count(boolean trashed) {
        return dao.count(EntityPhoto.class, Cnd.where("trashed", "=", trashed));
    }

    public List<EntityPhoto> query(int startRow, int endRow, boolean trashed) {
        return dao.query(EntityPhoto.class, Cnd.where("trashed", "=", trashed),
                new ExplicitPager(startRow, endRow - startRow));
    }

    public EntityPhoto savePhotoInDb(EntityFile parent, String key, String name) {
        try {
            COSObject cosObj = cosClient.getObject(bucket, key);
            String contentType = cosObj.getObjectMetadata().getContentType();
            if (contentType.contains("video")) {
                return saveVideo(parent, key, name);
            }
            File tmpFile = File.createTempFile(name, "" + Math.random());
            Streams.writeAndClose(new FileOutputStream(tmpFile), cosObj.getObjectContent());
            return saveFileInRepository(tmpFile, key, Files.getMajorName(name));
        } catch (IOException e) {
            log.error("Can't save file " + key, e);
        }

        return null;
    }

    private EntityPhoto saveVideo(EntityFile parent, String key, String name) {
        // todo: 处理视频
        return null;
    }

    public EntityPhoto saveFileInRepository(File file, String key, String name) {
        try {
            final BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null; // 不是正确的图片
            }

            EntityPhoto samePhoto = sameFileExist(file);
            if (samePhoto != null) {
                log.info("与图片 " + samePhoto.getId() + " 重复，file " + name + " 被抛弃！");
                cosClient.deleteObject(bucket, key);
                return samePhoto;
            }
            EntityBlockPicture blackPhoto = inBlackList(file);
            EntityPhoto photo = new EntityPhoto();
            photo.setSize(file.length());

            photo.setMd5(Lang.md5(file));
            photo.setSha1(Lang.sha1(file));

            photo.setWidth(image.getWidth());
            photo.setHeight(image.getHeight());
            setDateFromExif(file, photo);
            if (blackPhoto != null) {
                log.info("在黑名单里面，转移到回收站！");
                photo.setTrashed(true);
                dao.delete(blackPhoto);
            }
            log.info("Saving photo " + photo.getId());
            photo = dao.insert(photo);

            log.info("Photo:" + photo);
            cosClient.copyObject(bucket, key, bucket, photo.getName());
            cosClient.deleteObject(bucket, key);
/*
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket, photo.getName(), file);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("Cos put result:" + putObjectResult);
*/
            // Files.copyFile(file, pool.returnFile(photo.getId(), ".jpg"));
            file.delete();
            return photo;
        } catch (IOException e) {
            return null;
        }
    }

    private EntityPhoto sameFileExist(File file) {
        String md5 = Lang.md5(file);
        String sha1 = Lang.sha1(file);
        EntityPhoto existPhoto = dao.fetch(EntityPhoto.class,
                Cnd.where("md5", "=", md5).and("sha1", "=", sha1).and("size", "=", file.length()));

        return existPhoto;
    }

    private EntityBlockPicture inBlackList(File file) {
        String md5 = Lang.md5(file);
        String sha1 = Lang.sha1(file);
        EntityBlockPicture existPhoto = dao.fetch(EntityBlockPicture.class,
                Cnd.where("md5", "=", md5).and("sha1", "=", sha1).and("size", "=", file.length()));

        return existPhoto;
    }

    private void setDateFromExif(File file, EntityPhoto photo) {
        photo.setTakenDate(new Date(file.lastModified()));
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
            String requestStr =
                    "key=" + qqlbsKey + "&location=" + latitude + "," + longtitude;

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

    public void init() {
        qqlbsKey = conf.get("qqlbsKey");
        qqlbsToken = conf.get("qqlbsToken");
        bucket = conf.get("cos.bucket");
    }
}
