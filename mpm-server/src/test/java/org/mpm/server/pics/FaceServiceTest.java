package org.mpm.server.pics;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class FaceServiceTest {

    @Autowired
    FaceService faceService;
    @Autowired
    Dao dao;

    @Test
    void testFaceService() throws TencentCloudSDKException {
        // faceService.createFaceGroupIfNotExists();
        // faceService.detectFaceIn(dao.fetch(EntityPhoto.class, "vuu2hd8abujcvro2jt5e4bqlsg"));
        // faceService.detectFaceIn(dao.fetch(EntityPhoto.class, "vpbts6incmgm7pr2osfh5lbkkm"));
        // faceService.detectFaceIn(dao.fetch(EntityPhoto.class, "tafe7lm1luggepatqiib0gh49v"));
        faceService.detectFaceIn(dao.fetch(EntityPhoto.class, "vtqckbbjs4j0uq8vch1cesue9e"));

    }
}