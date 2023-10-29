package org.mpm.server.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qcloud.cos.exception.CosServiceException;
import feign.FeignException;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.pics.PicsService;
import org.mpm.server.util.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class CosRemoteServiceTest {

    @Autowired
    PicsService picsService;

    @Test
    void testGetImageInfo() throws IOException {
        assertThrows(CosServiceException.class, () -> picsService.getImageInfo("/small/xx"));

        Map info = picsService.getImageInfo("/small/rbll8savnmhrgr17nmc14mf50v");
        log.info("info {}", info);
        assertEquals(1280, MyUtils.parseInt(MyUtils.cell(info, "width"), 0));
        assertEquals(720, MyUtils.parseInt(MyUtils.cell(info, "height"), 0));
    }

    @Test
    void testGetExif() {
        assertThrows(FeignException.NotFound.class, () -> picsService.getExifInfo("xx"));
        Map info = picsService.getExifInfo("/small/rbll8savnmhrgr17nmc14mf50v");
        assertEquals("no exif data", info.get("error"));
        info = picsService.getExifInfo("/small/2au5dajq5ig04quu5r55vr918h");
        assertNull(info.get("error"));
        assertEquals("22/1 34/1 3954/100", MyUtils.cell(info, "GPSLatitude.val"));
    }
}