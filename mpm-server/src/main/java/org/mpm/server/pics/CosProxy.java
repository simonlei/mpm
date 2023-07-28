package org.mpm.server.pics;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@RestController
public class CosProxy {

    @Autowired
    COSClient cosClient;
    @Value("${cos.bucket}")
    String bucket;

    @GetMapping(value = "/cos/**")
    @ResponseBody
    Object proxyCosImage(HttpServletRequest req, HttpServletResponse resp) {
        // /cos/small/key?thumb=123
        // requestURI /cos/small/key
        // queryString thumb=123

        String key = req.getRequestURI().substring("/cos".length());
        log.info("The key is " + key);
        String param = req.getQueryString();
        log.info("The param is " + param);
        try {
            if ("exif".equals(param)) return proxyImageInfo(key, "exif");
            if ("imageInfo".equals(param)) return proxyImageInfo(key, "imageInfo");
            return getCosResult(key);
        } catch (Exception e) {
            log.error("Can't proxy cos image", e);
            return null;
        }
    }

    private ResponseEntity<byte[]> getCosResult(String key) throws IOException {
        COSObject cosObj = cosClient.getObject(bucket, key);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(31536000)))
                .contentType(MediaType.parseMediaType(cosObj.getObjectMetadata().getContentType()))
                .body(Streams.readAll(cosObj.getObjectContent()));
    }

    private Object proxyImageInfo(String key, String rule) throws IOException {
        GetObjectRequest getObj = new GetObjectRequest(bucket, key);
        getObj.putCustomQueryParameter(rule, null);
        COSObject object = cosClient.getObject(getObj);
        COSObjectInputStream objectContent = object.getObjectContent();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(31536000)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Streams.readAll(objectContent));
    }
}
