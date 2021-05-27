package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mpm.server.BaseTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PicsModuleTest extends BaseTest {

    @Test
    public void testGetCoverName() {
        String key = "https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295.MP4";
        assertEquals("https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295",
                key.substring(0, key.lastIndexOf(".")));
    }
}
