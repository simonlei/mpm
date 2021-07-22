package org.mpm.server.pics;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.util.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GisService {

    @Autowired
    GisRemoteService gisRemoteService;
    public static final int GEO_API_LIMIT = 8000;
    private int tokens = GEO_API_LIMIT;

    public String getAddress(Double latitude, Double longitude) {
        takeToken();
        try {
            Map result = gisRemoteService.getAddress(latitude, longitude);
            log.info("qqlbs result:" + result);
            return MyUtils.cell(result, "result.address");
        } catch (Exception e) {
            log.error("Can't get lbs address", e);
            return "";
        }
    }

    public void refreshToken() {
        log.info("Reset token from " + tokens);
        tokens = GEO_API_LIMIT;
    }

    public boolean hasToken() {
        return tokens > 0;
    }

    public void takeToken() {
        tokens--;
    }
}
