package org.mpm.server.metas;

import com.tencent.cloud.CosStsClient;
import java.io.IOException;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mpm.server.util.MyUtils;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
@Slf4j
public class ConfigDataSource {

    @Inject
    PropertiesProxy conf;

    @At("/tmpCredential")
    @Ok("raw")
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
        config.put("SecretId", conf.get("cos.secretId"));
        config.put("SecretKey", conf.get("cos.secretKey"));
        config.put("bucket", conf.get("cos.bucket"));
        config.put("region", conf.get("cos.region"));
        config.put("durationSeconds", 7200);
        config.put("allowPrefix", "upload/*");
        config.put("allowActions", allowActions);
        JSONObject credential = CosStsClient.getCredential(config);
        log.info("Credential is " + credential.toString());
        return credential.toString();
    }

    @At("/config")
    @Ok("raw")
    public String getConfigJs() {
        PropertiesProxy conf = MyUtils.getByType(PropertiesProxy.class);
        String bucket = conf.get("cos.bucket");
        String region = conf.get("cos.region");
        String s = "var bucket=\"" + bucket + "\"; \nvar region=\"" + region + "\";\n";
        log.info("Config is:" + s);
        return s;
    }

    public NutMap fetchConfig() {
        NutMap result = new NutMap();

        PropertiesProxy conf = MyUtils.getByType(PropertiesProxy.class);
        String bucket = conf.get("cos.bucket");
        String region = conf.get("cos.region");
        result.setv("thumbUrl", String.format("https://%s.cos.%s.myqcloud.com/", bucket, region));
        return result;
    }
}
