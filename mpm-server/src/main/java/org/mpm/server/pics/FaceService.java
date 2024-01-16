package org.mpm.server.pics;

import com.tencentcloudapi.iai.v20200303.models.FaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.cron.PhotoTaskScanner;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FaceService {

    @Autowired
    Dao dao;
    @Autowired
    PhotoTaskScanner photoTaskScanner;

    /**
     * Detect faces in image.
     */
    public void detectFaces() {
        photoTaskScanner.scanPhotoDoTask("lastCheckDetectedFaces", photo -> {
            try {
                detectFaceIn(photo);
            } catch (Exception e) {
                log.error("Can't detect face, photo:" + photo.getId(), e);
            }
        }, 100, false, null);
    }

    private void detectFaceIn(EntityPhoto photo) {
        FaceInfo[] faceInfos = detectFacesInPhoto(photo);
        if (faceInfos.length == 0) {
            return;
        }
        createFaceGroupIfNotExists();
        for (FaceInfo faceInfo : faceInfos) {
            // addFactToGroup(); has duplicate faces?
            // add relation between face and photo
        }
    }

    private FaceInfo[] detectFacesInPhoto(EntityPhoto photo) {
        return null;
    }

    private void createFaceGroupIfNotExists() {

    }
}
