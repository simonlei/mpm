package org.mpm.server.cron;

import org.mpm.server.pics.FaceService;
import org.mpm.server.pics.GeoChecker;
import org.mpm.server.pics.GisService;
import org.mpm.server.pics.PicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CronService {

    @Autowired
    GisService gisService;
    @Autowired
    GeoChecker geoChecker;

    @Autowired
    PicsService picsService;
    @Autowired
    FaceService faceService;

    // @Scheduled(cron = "0 0 0 * * *")
    void refreshGisToken() {
        gisService.refreshToken();
    }

    // @Scheduled(fixedDelay = 5000)
    void checkPhotoGeos() {
        if (gisService.hasToken()) {
            geoChecker.checkPhotoGeos();
        }
    }

    // @Scheduled(fixedDelay = 5000)
    void checkPhotoDates() {
        /*
         geoChecker.checkPhotoDates();
         geoChecker.generateSmallPhotos();
        */
        geoChecker.clearDuplicateDescs();
        // 还要再查一遍拿到了gps信息但是没获取地址的
        // select * from t_photos where latitude is not null and address is null limit 10;
    }

    @Scheduled(fixedDelay = 5000)
    void detectFaces() {
        faceService.detectFaces();
    }
}
