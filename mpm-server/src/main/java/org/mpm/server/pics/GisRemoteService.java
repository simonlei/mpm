package org.mpm.server.pics;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Map;
import org.mpm.server.pics.GisRemoteService.GisConfig;
import org.nutz.lang.Lang;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "gisRemoteService", url = "https://apis.map.qq.com/ws/geocoder",
        configuration = GisConfig.class)
//@Service
public interface GisRemoteService {

    @GetMapping("/v1?key=${qqlbsKey}&location={latitude}%2c{longitude}&sig")
    Map getAddress(@PathVariable Double latitude, @PathVariable Double longitude);

    class GisConfig implements RequestInterceptor {

        @Value("${qqlbsKey}")
        String qqlbsKey;
        @Value("${qqlbsToken}")
        String qqlbsToken;

        @Override
        public void apply(RequestTemplate template) {
            String url = template.url();
            String location = template.queries().get("location").iterator().next();
            location = location.replace("%2c", ",");
            String requestStr = "key=" + qqlbsKey + "&location=" + location;
            String sig = Lang.md5("/ws/geocoder/v1?" + requestStr + qqlbsToken);

            template.uri(url + "=" + sig);
        }
    }
}
