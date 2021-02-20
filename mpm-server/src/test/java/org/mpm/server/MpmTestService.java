package org.mpm.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.pics.PicsDataSource;
import org.nutz.boot.test.NutzBootTest;
import org.nutz.ioc.loader.annotation.Inject;

@NutzBootTest
@Slf4j
public class MpmTestService {

    @Inject
    PicsDataSource picsDataSource;

    @Test
    public void testNothing() throws UnsupportedEncodingException {
        Integer value = picsDataSource.getOldId("id=96&name=xxx");
        assertEquals(value, 96);
    }
}