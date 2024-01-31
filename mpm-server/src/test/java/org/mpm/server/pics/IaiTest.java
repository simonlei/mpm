package org.mpm.server.pics;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.utils.Base64;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.DetectFaceResponse;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.nutz.lang.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class IaiTest {

    @Autowired
    IaiClient iaiClient;
    @Autowired
    COSClient cosClient;
    @Value(value = "${cos.bucket}")
    String bucket;

    @Test
    void testFaceDetect() throws TencentCloudSDKException {
        DetectFaceRequest detectFaceRequest = new DetectFaceRequest();
        GetObjectRequest objectRequest = new GetObjectRequest(bucket, "/small/tafe7lm1luggepatqiib0gh49v");
        objectRequest.putCustomQueryParameter("imageMogr2/format/jpeg", null);
        COSObject object = cosClient.getObject(objectRequest);

        detectFaceRequest.setImage(Base64.encodeAsString(Streams.readBytesAndClose(object.getObjectContent())));
        detectFaceRequest.setMaxFaceNum(10L);
        DetectFaceResponse detectFaceResponse = iaiClient.DetectFace(detectFaceRequest);
        HashMap<String, String> result = new HashMap<>();
        detectFaceResponse.toMap(result, "");
        log.info("result: {}", result);
    }
}
