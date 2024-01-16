package org.mpm.server.pics;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.utils.Base64;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreateGroupRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceResponse;
import com.tencentcloudapi.iai.v20200303.models.FaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mpm.server.cron.PhotoTaskScanner;
import org.mpm.server.entity.EntityFace;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.entity.EntityPhotoFaceInfo;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FaceService {

    @Autowired
    Dao dao;
    @Autowired
    PhotoTaskScanner photoTaskScanner;
    @Autowired
    IaiClient iaiClient;
    @Value("${isDev}")
    Boolean isDev;
    @Value(value = "${cos.bucket}")
    String bucket;
    @Autowired
    COSClient cosClient;

    /**
     * Detect faces in image.
     */
    public void detectFaces() {
        createFaceGroupIfNotExists();
        photoTaskScanner.scanPhotoDoTask("lastCheckDetectedFaces", photo -> {
            try {
                detectFaceIn(photo);
            } catch (Exception e) {
                log.error("Can't detect face, photo:" + photo.getId(), e);
            }
        }, 100, false, null);
    }

    private void detectFaceIn(EntityPhoto photo) throws TencentCloudSDKException {
        FaceInfo[] faceInfos = detectFacesInPhoto(photo);
        if (faceInfos == null || faceInfos.length == 0) {
            return;
        }
        dao.clear(EntityPhotoFaceInfo.class, Cnd.where("photo_id", "=", photo.getId()));
        for (FaceInfo faceInfo : faceInfos) {
            EntityPhotoFaceInfo info = recordFaceInfo(photo, faceInfo);
            addFaceToGroup(photo, info);
        }
    }

    private void addFaceToGroup(EntityPhoto photo, EntityPhotoFaceInfo faceInfo) throws TencentCloudSDKException {
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(getGroupName());
        req.setImage(getImage(photo, faceInfo));
        req.setPersonId("" + photo.getId());
        req.setPersonName("" + photo.getId());
        req.setUniquePersonControl(1L);
        CreatePersonResponse createPersonResponse = iaiClient.CreatePerson(req);
        String faceId = createPersonResponse.getFaceId();
        String similarPersonId = createPersonResponse.getSimilarPersonId();
        EntityFace face;
        if (Strings.isBlank(similarPersonId)) {
            // create new face
            face = EntityFace.builder().faceId(faceId).personId(photo.getId()).build();
            face = dao.insert(face, true, false, false);
        } else {
            face = dao.fetch(EntityFace.class, Cnd.where("person_id", "=", similarPersonId));
        }
        faceInfo.setFaceId(face.getId());
        dao.updateIgnoreNull(faceInfo);
    }

    private EntityPhotoFaceInfo recordFaceInfo(EntityPhoto photo, FaceInfo faceInfo) {
        // face info 应该记录下来，到时候可以在页面上点选对应的人头
        EntityPhotoFaceInfo photoFaceInfo = EntityPhotoFaceInfo.builder().photoId(photo.getId()).x(faceInfo.getX())
                .y(faceInfo.getY()).width(faceInfo.getWidth()).height(faceInfo.getHeight()).build();
        return dao.insert(photoFaceInfo);
    }

    private FaceInfo[] detectFacesInPhoto(EntityPhoto photo) throws TencentCloudSDKException {
        String image = getImage(photo, null);

        DetectFaceRequest detectFaceRequest = new DetectFaceRequest();
        detectFaceRequest.setImage(image);
        detectFaceRequest.setMaxFaceNum(10L);
        DetectFaceResponse detectFaceResponse = iaiClient.DetectFace(detectFaceRequest);
        return detectFaceResponse.getFaceInfos();
    }

    /**
     * 如果face为空，则从cos上获取整个图片，否则 cut 一下
     */
    private String getImage(EntityPhoto photo, EntityPhotoFaceInfo face) {
        GetObjectRequest objectRequest = new GetObjectRequest(bucket, "/small/" + photo.getName());
        String param = "imageMogr2/format/jpeg";
        if (face != null) {
            param = "imageMogr2/cut/" + face.getWidth()
                    + "x" + face.getHeight()
                    + "x" + face.getX()
                    + "x" + face.getY() + "|" + param;
        }
        objectRequest.putCustomQueryParameter(param, null);
        COSObject object = cosClient.getObject(objectRequest);
        return Base64.encodeAsString(Streams.readBytesAndClose(object.getObjectContent()));
    }

    private void createFaceGroupIfNotExists() {
        try {
            CreateGroupRequest req = new CreateGroupRequest();
            req.setGroupId(getGroupName());
            req.setGroupName(getGroupName());
            iaiClient.CreateGroup(req);
        } catch (Exception e) {
            // do nothing.
        }
    }

    @NotNull
    private String getGroupName() {
        return "faceGroup" + (isDev ? "-dev" : "");
    }
}
