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
import com.tencentcloudapi.iai.v20200303.models.DeletePersonFromGroupRequest;
import com.tencentcloudapi.iai.v20200303.models.DeletePersonFromGroupResponse;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceResponse;
import com.tencentcloudapi.iai.v20200303.models.FaceInfo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mpm.server.cron.PhotoTaskScanner;
import org.mpm.server.entity.EntityFace;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.entity.EntityPhotoFaceInfo;
import org.mpm.server.pics.FaceController.FaceUpdateParam;
import org.mpm.server.util.DaoUtil;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Trans;
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

    @PostConstruct
    public void init() {
        createFaceGroupIfNotExists();
    }


    /**
     * Detect faces in image.
     */
    public void detectFaces() {
        photoTaskScanner.scanPhotoDoTask("lastCheckDetectedFaces", photo -> {
            try {
                detectFaceIn(photo);
            } catch (Exception e) {
                if (!Lang.equals("图片中没有人脸。", e.getMessage())) {
                    log.error("Can't detect face, photo:" + photo.getId(), e);
                }
            }
        }, 100, true, null);
    }

    void detectFaceIn(EntityPhoto photo) throws TencentCloudSDKException {
        FaceInfo[] faceInfos = detectFacesInPhoto(photo);
        if (faceInfos == null || faceInfos.length == 0) {
            return;
        }
        dao.clear(EntityPhotoFaceInfo.class, Cnd.where("photoId", "=", photo.getId()));
        for (FaceInfo faceInfo : faceInfos) {
            try {
                if (faceInfo.getWidth() <= 64 || faceInfo.getHeight() <= 64
                        || faceInfo.getX() < 0 || faceInfo.getY() < 0) {
                    continue;
                }
                EntityPhotoFaceInfo info = recordFaceInfo(photo, faceInfo);
                addFaceToGroup(photo, info);
            } catch (Exception e) {
                if (!Lang.equals("图片中没有人脸。", e.getMessage())) {
                    log.info("can't add face {}x{}x{}x{} to group, photo: {}",
                            faceInfo.getWidth(), faceInfo.getHeight(),
                            faceInfo.getX(), faceInfo.getY(), photo.getId(), e);
                }
            }
        }
    }

    private void addFaceToGroup(EntityPhoto photo, EntityPhotoFaceInfo faceInfo) throws TencentCloudSDKException {
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(getGroupName());
        req.setImage(getImage(photo, faceInfo));
        // 一张照片里面可能有多个 face，所以 person id 应该是photo.id+faceInfo.id
        String personId = photo.getId() + "-" + faceInfo.getId();
        req.setPersonId(personId);
        req.setPersonName(personId);
        req.setUniquePersonControl(1L);
        req.setNeedRotateDetection(1L);
        CreatePersonResponse createPersonResponse = iaiClient.CreatePerson(req);
        String faceId = createPersonResponse.getFaceId();
        String similarPersonId = createPersonResponse.getSimilarPersonId();
        log.info("face info {} 's similar person id {} ", photo.getName(), similarPersonId);
        // 有可能 entity face 被删除了的情况，这时要创建新的 face
        EntityFace face = Strings.isBlank(similarPersonId) ? null
                : dao.fetch(EntityFace.class, Cnd.where("personId", "=", similarPersonId));
        if (face == null) {
            // create new face
            face = EntityFace.builder().faceId(faceId).personId(personId).build();
            face = dao.insert(face, true, false, false);
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
        detectFaceRequest.setNeedRotateDetection(1L);
        DetectFaceResponse detectFaceResponse = iaiClient.DetectFace(detectFaceRequest);
        return detectFaceResponse.getFaceInfos();
    }

    /**
     * 如果face为空，则从cos上获取整个图片，否则 cut 一下
     */
    private String getImage(EntityPhoto photo, EntityPhotoFaceInfo face) {
        COSObject object = getFaceFromCos(photo, face);
        return Base64.encodeAsString(Streams.readBytesAndClose(object.getObjectContent()));
    }

    private COSObject getFaceFromCos(EntityPhoto photo, EntityPhotoFaceInfo face) {
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
        return object;
    }

    void createFaceGroupIfNotExists() {
        try {
            CreateGroupRequest req = new CreateGroupRequest();
            req.setGroupId(getGroupName());
            req.setGroupName(getGroupName());
            iaiClient.CreateGroup(req);
        } catch (Exception e) {
            // do nothing.
            // log.error("Can't create face group", e);
        }
    }

    @NotNull
    private String getGroupName() {
        return "faceGroup" + (isDev ? "-dev" : "");
    }

    public List<NutMap> getFaces(Boolean showHidden) {
        Sql sql = Sqls.create("""
                select personId, i.faceId, name, selectedFace, collected, hidden, count(*) as count
                from photo_face_info i
                left join t_face on t_face.id=i.faceId
                where personId is not null $showHidden
                group by faceId
                order by collected desc, count(*) desc
                limit 100
                """);
        sql.setVar("showHidden", showHidden ? "" : "and t_face.hidden=false");
        log.info("getFaces: {}", sql);
        return DaoUtil.fetchMaps(dao, sql);
    }

    public byte[] getFaceImg(int id, Long photoId) {
        EntityPhotoFaceInfo faceInfo = dao.fetch(EntityPhotoFaceInfo.class,
                photoId < 0 ? Cnd.where("faceId", "=", id).orderBy("height", "desc")
                        : Cnd.where("id", "=", photoId));
        EntityPhoto photo = dao.fetch(EntityPhoto.class, Cnd.where("id", "=", faceInfo.getPhotoId()));
        getWiderFace(photo, faceInfo);
        COSObject faceFromCos = getFaceFromCos(photo, faceInfo);
        return Streams.readBytesAndClose(faceFromCos.getObjectContent());
    }

    /**
     * 扩大当前人脸的周边
     */
    private void getWiderFace(EntityPhoto photo, EntityPhotoFaceInfo faceInfo) {
        long padX = Double.valueOf(faceInfo.getWidth() * 0.3).longValue();
        long padY = Double.valueOf(faceInfo.getHeight() * 0.3).longValue();
        long x = faceInfo.getX() - padX;
        long y = faceInfo.getY() - padY;
        long w = x + faceInfo.getWidth() + 2 * padX;
        long h = y + faceInfo.getHeight() + 2 * padY;
        long x2 = 0;
        long y2 = 0;
        long w2 = photo.getWidth();
        long h2 = photo.getHeight();

        faceInfo.setX(Math.max(x, x2));
        faceInfo.setY(Math.max(y, y2));
        faceInfo.setWidth(Math.min(w, w2) - faceInfo.getX());
        faceInfo.setHeight(Math.min(h, h2) - faceInfo.getY());
    }

    public boolean updateFace(FaceUpdateParam face) {
        EntityFace entityFace = dao.fetch(EntityFace.class, face.faceId);
        if (entityFace == null) {
            return false;
        }
        entityFace.setName(face.getName());
        if (face.getSelectedFace() != null) {
            // 设置对应的图片当中的人脸来当做默认人脸
            EntityPhotoFaceInfo faceInfo = dao.fetch(EntityPhotoFaceInfo.class,
                    Cnd.where("photoId", "=", face.getSelectedFace()).and("faceId", "=", face.getFaceId()));
            entityFace.setSelectedFace(faceInfo.getId());
        }
        if (face.getHidden() != null) {
            entityFace.setHidden(face.getHidden());
        }
        if (face.getCollected() != null) {
            entityFace.setCollected(face.getCollected());
        }
        dao.updateIgnoreNull(entityFace);
        return true;
    }

    public List<NutMap> getFacesWithName() {
        return DaoUtil.fetchMaps(dao, "select id as faceId, name from t_face where name is not null");
    }

    public Boolean mergeFace(Long from, Long to) throws TencentCloudSDKException {
        // 这里也要把腾讯云上的 face 给删掉
        DeletePersonFromGroupRequest req = new DeletePersonFromGroupRequest();
        req.setGroupId(getGroupName());
        EntityFace face = dao.fetch(EntityFace.class, from);
        req.setPersonId(face.getPersonId());
        DeletePersonFromGroupResponse resp = iaiClient.DeletePersonFromGroup(req);
        log.info("Delete person from group response:{}", resp);
        Trans.exec(() -> {
            dao.execute(Sqls.create("delete from t_face where id=@from").setParam("from", from));
            dao.execute(Sqls.create("update photo_face_info set faceId=@to where faceId=@from")
                    .setParam("from", from)
                    .setParam("to", to));
        });
        return true;
    }

    public List<NutMap> getFacesForPhoto(Long id) {
        if (id == null) {
            return new ArrayList<>();
        }
        return DaoUtil.fetchMaps(dao, """
                select pf.faceId, x, y, width, height, name
                from photo_face_info pf
                inner join t_face f on f.id=pf.faceId
                where pf.photoId=
                """ + id);
    }
}
