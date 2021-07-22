package org.mpm.server.cron;

import org.mpm.server.pics.GeoChecker;
import org.mpm.server.pics.GisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CronService {

    @Autowired
    GisService gisService;
    @Autowired
    GeoChecker geoChecker;

    @Scheduled(cron = "0 0 0 * * *")
    void refreshGisToken() {
        gisService.refreshToken();
    }

    @Scheduled(fixedDelay = 5000)
    void checkPhotos() {
        geoChecker.checkPhotoGeos();
    }
}
