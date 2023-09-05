package org.mpm.server.remote;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.image.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CosServiceTest {

    @Autowired
    COSClient cosClient;

    @Test
    void testLabel() {
        ImageLabelRequest labelRequest = new ImageLabelRequest();
        labelRequest.setBucketName("photos-1251477527");
        labelRequest.setObjectKey("origin/007hm1amr8gcup140u3f33dtnm");

        ImageLabelResponse label = cosClient.getImageLabel(labelRequest);
        List<Label> recognitionResult = label.getRecognitionResult();
        for (Label l : recognitionResult) {
            System.out.println(l.getConfidence() + " : " + l.getName());
        }
    }

    // @Test
    void testLabel2() {
        ImageLabelV2Request labelRequest2 = new ImageLabelV2Request();

        labelRequest2.setBucketName("photos-1251477527");
        labelRequest2.setObjectKey("origin/007hm1amr8gcup140u3f33dtnm");
        labelRequest2.setScenes("album");

        ImageLabelV2Response label2 = cosClient.getImageLabelV2(labelRequest2);
        List<LabelV2> recognitionResult2 = label2.getAlbumLabels();
        for (LabelV2 l : recognitionResult2) {
            System.out.println(l.getConfidence() + " : " + l.getName());
        }

    }
}
