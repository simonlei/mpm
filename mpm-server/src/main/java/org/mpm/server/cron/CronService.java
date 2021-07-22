package org.mpm.server.cron;

import org.mpm.server.pics.GisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronService {

    @Autowired
    GisService gisService;

    @Scheduled(cron = "0 0 0 * * *")
    void refreshGisToken() {
        gisService.refreshToken();
    }
}
