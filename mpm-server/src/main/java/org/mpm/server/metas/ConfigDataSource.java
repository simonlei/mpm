package org.mpm.server.metas;

import com.tencent.cloud.CosStsClient;
import java.io.IOException;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mpm.server.entity.EntityFile;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String getConfigJs() {
        log.info("Dao is {}", dao);
        log.info("Config {}", dao.fetch(EntityFile.class, 1));
        String s = "var bucket=\"" + bucket + "\"; \nvar region=\"" + region + "\";\n";
        log.info("Config is:" + s);
        return s;
    }

    // used in client
    public NutMap fetchConfig() {
        NutMap result = new NutMap();
        result.setv("thumbUrl", String.format("https://%s.cos.%s.myqcloud.com/", bucket, region));
        return result;
    }

    // used in client
    public NutMap authPassword(String passwd) {
        if (password.equals(passwd)) {
            return Lang.map("ok", "ok");
        }
        return new NutMap();
    }
}
