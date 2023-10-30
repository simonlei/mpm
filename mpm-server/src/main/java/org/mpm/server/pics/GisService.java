package org.mpm.server.pics;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.util.MyUtils;
import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GisService {

    public static final int GEO_API_LIMIT = 8000;
    @Value("${qqlbsKey}")
    String qqlbsKey;
    @Value("${qqlbsToken}")
    String qqlbsToken;
    private int tokens = GEO_API_LIMIT;

    public String getAddress(Double latitude, Double longitude) {
        takeToken();
        try {
            Map result = getAddressFromRemote(latitude, longitude);
            log.info("qqlbs result:" + result);
            return MyUtils.cell(result, "result.address");
        } catch (Exception e) {
            log.error("Can't get lbs address", e);
            return "";
        }
    }

    private Map getAddressFromRemote(Double latitude, Double longitude) {
        String requestStr = "key=" + qqlbsKey + "&location=" + latitude + "," + longitude;
        String sig = Lang.md5("/ws/geocoder/v1?" + requestStr + qqlbsToken);
        String url = "https://apis.map.qq.com/ws/geocoder/v1?key=" + qqlbsKey
                + "&location=" + latitude + "%2c" + longitude + "&sig=" + sig;
        log.info("Getting gis info from " + url);
        return (Map) Json.fromJson(Http.get(url).getContent());
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
