package org.mpm.server.pics;

import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.mapl.Mapl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GisService {

    @Value("${qqlbsKey}")
    String qqlbsKey;
    @Value("${qqlbsToken}")
    String qqlbsToken;

    public static final int GEO_API_LIMIT = 8000;
    private int tokens = GEO_API_LIMIT;
    
    public String getAddress(double latitude, double longtitude) {
        takeToken();
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

    public void refreshToken() {
        log.info("Reset token from " + tokens);
        tokens = GEO_API_LIMIT; // reset token everyday
    }

    public boolean hasToken() {
        return tokens > 0;
    }

    public void takeToken() {
        tokens--;
    }
}
