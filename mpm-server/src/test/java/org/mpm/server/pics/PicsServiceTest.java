package org.mpm.server.pics;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.BaseTest;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Slf4j
public class PicsServiceTest extends BaseTest {

    @Autowired
    COSClient cosClient;
    @Autowired
    PicsService picsService;
    @Autowired
    Dao dao;

    @Test
    public void testGetCoverName() {
        String key = "https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295.MP4";
        assertEquals("https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295",
                key.substring(0, key.lastIndexOf(".")));
    }

    //@Test
    private void transferVideo() {
        // String bucket = "photosdev-1251477527";
        String bucket = "Transfer only once, do not run again";
        ObjectListing objectListing = cosClient.listObjects(bucket, "video/");
        List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        for (COSObjectSummary summary : objectSummaries) {
            String key = summary.getKey();
            log.info(key);
            String name = key.substring(6);
            String tmpKey = "tmp_video/" + name + ".mp4";
            cosClient.copyObject(bucket, key, bucket, tmpKey);
            cosClient.deleteObject(bucket, key);
            cosClient.copyObject(bucket, tmpKey, bucket, "video/" + name + ".mp4");
            cosClient.deleteObject(bucket, tmpKey);
            log.info(name);
        }
    }

    @Test
    void testParseGps() {
        assertNull(picsService.parseGps(null));
        assertNull(picsService.parseGps("not valid gps"));
        assertEquals(22.18611111111111, picsService.parseGps("22/1 11/1 1000/100"));
    }

    @Test
    void testSetDate() {
        EntityPhoto p = dao.fetch(EntityPhoto.class, 20);
        picsService.setInfosFromCos("origin/" + p.getName(), p);
        assertEquals("Sat Sep 13 10:35:08 CST 2014", p.getTakenDate().toString());
    }

    @Test
    void testSnapshot() {
        picsService.generateSmallPic("/origin/3e0ka5dqbmit8odd3nfifc80k6", "3e0ka5dqbmit8odd3nfifc80k6");
    }
}
