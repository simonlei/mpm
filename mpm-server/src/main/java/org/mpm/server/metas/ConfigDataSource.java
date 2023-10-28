package org.mpm.server.metas;

import com.tencent.cloud.CosStsClient;
import java.io.IOException;
import java.util.TreeMap;
import java.util.UUID;
import javax.servlet.ServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityMeta;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ConfigDataSource {

    @Value("${cos.bucket}")
    String bucket;
    @Value("${cos.region}")
    String region;
    @Value("${cos.secretId}")
    String secretId;
    @Value("${cos.secretKey}")
    String secretKey;
    @Value("${password}")
    String password;
    @Value("${isDev}")
    String isDev;
    @Autowired
    Dao dao;

    // used in client
    @GetMapping("/tmpCredential")
    public String getTmpCredential() throws IOException {
        TreeMap<String, Object> config = new TreeMap<>();
        String[] allowActions = new String[]{
                //简单上传操作
                "name/cos:PutObject",
                //表单上传对象
                "name/cos:PostObject",
                //分块上传：初始化分块操作
                "name/cos:InitiateMultipartUpload",
                //分块上传：List 进行中的分块上传
                "name/cos:ListMultipartUploads",
                //分块上传：List 已上传分块操作
                "name/cos:ListParts",
                //分块上传：上传分块块操作
                "name/cos:UploadPart",
                //分块上传：完成所有分块上传操作
                "name/cos:CompleteMultipartUpload",
                //取消分块上传操作
                "name/cos:AbortMultipartUpload"
        };
        config.put("SecretId", secretId);
        config.put("SecretKey", secretKey);
        config.put("bucket", bucket);
        config.put("region", region);
        config.put("durationSeconds", 7200);
        config.put("allowPrefix", "upload/*");
        config.put("allowActions", allowActions);
        JSONObject credential = CosStsClient.getCredential(config);
        log.info("Credential is " + credential.toString());
        return credential.toString();
    }

    // used in client
    @GetMapping("/config")
    public String getConfigJs(ServletRequest request) {
        log.info("Dao is {}", dao);
        log.info("Real path is {}", request.getServletContext().getRealPath("/"));
        log.info("Config {}", dao.fetch(EntityFile.class, 1));
        String s = "var bucket=\"" + bucket + "\"; \nvar region=\"" + region + "\";\n";
        log.info("Config is:" + s);
        return s;
    }

    // used in client
    @GetMapping("/api/getConfig")
    public NutMap fetchConfig(@RequestParam("user") String user,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("signature") String signature) {
        log.info("user {} timestamp {} signature {}", user, timestamp, signature);
        NutMap result = new NutMap();
        result.setv("baseUrl", String.format("https://%s.cos.%s.myqcloud.com/", bucket, region));
        result.setv("thumbUrl", "/thumb/");
        result.setv("bucket", bucket);
        result.setv("region", region);
        result.setv("isDev", isDev);
        if (user != null && timestamp != null && signature != null) {
            result.setv("isLogin", checkSignature(user, timestamp, signature));
        }
        // result.setv("isLogin", "true".equals(CookieTools.getCookieValueByName(req, CookieTools.IS_LOGGED_IN)));
        return result;
    }

    private Boolean checkSignature(String user, String timestamp, String signature) {
        // todo: 检查 timestamp，定个有效期
        return Lang.equals(signature, calcSignature(user, timestamp));
    }

    private String calcSignature(String user, String timestamp) {
        EntityMeta token = dao.fetch(EntityMeta.class, "login_token");
        if (token == null) {
            token = EntityMeta.builder().key("login_token").value(UUID.randomUUID().toString()).build();
            dao.insert(token);
        }
        String calcSign = Lang.md5(user + token.getValue() + timestamp);
        return calcSign;
    }

    // used in client
    @PostMapping("/api/authPassword")
    public NutMap authPassword(@RequestBody NutMap userInfo) {
        if (password.equals(userInfo.get("password"))) {
            NutMap map = Lang.map("user", "admin"); //todo: add real login
            String timestamp = "" + System.currentTimeMillis();
            map.setv("ok", "ok").setv("timestamp", timestamp).setv("signature", calcSignature("admin", timestamp));
            return map;
        }
        return new NutMap();
    }
}
