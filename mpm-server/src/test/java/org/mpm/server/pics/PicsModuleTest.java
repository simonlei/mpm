package org.mpm.server.pics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mpm.server.BaseTest;

public class PicsModuleTest extends BaseTest {

    @Test
    public void testGetCoverName() {
        String key = "https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295.MP4";
        assertEquals("https://bucket.cos.ap-guangzhou.myqcloud.com/upload/M4H02295",
                key.substring(0, key.lastIndexOf(".")));
    }
}
