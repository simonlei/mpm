package org.mpm.server.pics;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

@Slf4j
@RestController
public class CosProxy {

    @Autowired
    COSClient cosClient;
    @Value("${cos.bucket}")
    String bucket;
    Properties styles;

    @PostConstruct
    void init() throws IOException {
        styles = new Properties();
        styles.load(getClass().getResourceAsStream("/cos_styles.properties"));
    }

    @GetMapping(value = "/cos/**")
    @ResponseBody
    Object proxyCosImage(HttpServletRequest req, HttpServletResponse resp) {
        // /cos/small/key?thumb=123
        // requestURI /cos/small/key
        // queryString thumb=123

        String key = req.getRequestURI().substring("/cos".length());
        String param = req.getQueryString();
        log.info("The origin key {} and param {}", key, param);
        if (key.contains("/thumb")) {
            if (param != null) {
                throw new Error("同时存在param和thum");
            }
            param = subParam(key);
            key = key.substring(0, key.length() - param.length() - 1);
            log.info("The parsed key {} and param {}", key, param);
        }
        try {
            if ("exif".equals(param)) return proxyImageInfo(key, "exif");
            if ("imageInfo".equals(param)) return proxyImageInfo(key, "imageInfo");
            return getCosResult(key, param);
        } catch (Exception e) {
            log.error("Can't proxy cos image", e);
            return null;
        }
    }

    String subParam(String key) {
        return key.substring(key.lastIndexOf("/thumb") + 1);
    }

    private ResponseEntity<byte[]> getCosResult(String key, String param) throws IOException {
        // COSObject cosObj = cosClient.getObject(bucket, key);

        GetObjectRequest getObj = new GetObjectRequest(bucket, key);
        getObj.putCustomQueryParameter(param, null);
        COSObject cosObj = cosClient.getObject(getObj);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(31536000)))
                .contentType(MediaType.parseMediaType(cosObj.getObjectMetadata().getContentType()))
                .body(Streams.readAll(cosObj.getObjectContent()));
    }

    private Object proxyImageInfo(String key, String rule) throws IOException {
        GetObjectRequest getObj = new GetObjectRequest(bucket, key);
        getObj.putCustomQueryParameter(rule, null);
        COSObject object = cosClient.getObject(getObj);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(31536000)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Streams.readAll(object.getObjectContent()));
    }
}
