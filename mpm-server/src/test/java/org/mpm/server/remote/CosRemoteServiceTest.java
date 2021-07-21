package org.mpm.server.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feign.FeignException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mpm.server.util.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CosRemoteServiceTest {

    @Autowired
    CosRemoteService cosRemoteService;

    @Test
    void testGetImageInfo() {
        assertThrows(FeignException.NotFound.class, () -> cosRemoteService.getImageInfo("xx"));
        Map info = cosRemoteService.getImageInfo("rbll8savnmhrgr17nmc14mf50v");
        assertEquals(1280, MyUtils.parseInt(MyUtils.cell(info, "width"), 0));
        assertEquals(720, MyUtils.parseInt(MyUtils.cell(info, "height"), 0));
    }

    @Test
    void testGetExif() {
        assertThrows(FeignException.NotFound.class, () -> cosRemoteService.getExifInfo("xx"));
        Map info = cosRemoteService.getExifInfo("rbll8savnmhrgr17nmc14mf50v");
        assertEquals("no exif data", info.get("error"));
        info = cosRemoteService.getExifInfo("2au5dajq5ig04quu5r55vr918h");
        assertNull(info.get("error"));
        assertEquals("22/1 34/1 3954/100", MyUtils.cell(info, "GPSLatitude.val"));
    }
}