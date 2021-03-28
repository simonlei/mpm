package org.mpm.server.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mpm.server.BaseTest;
import org.nutz.boot.test.junit4.NbJUnit4Runner;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;


@Slf4j
@IocBean
@RunWith(NbJUnit4Runner.class)
public class PhotoImporterDataSourceTest extends BaseTest {

    @Inject
    PhotoImporterDataSource photoImporterDataSource;

    @Test
    public void testImportPhoto() {
        String key = photoImporterDataSource
                .uploadFile("upload/1616940995641_tmpupload/七上1025义工/IMG_004.jpg");
        // upload/1616940995641_tmpupload/IMG_001.jpg
        log.info("Key is " + key);
        key = photoImporterDataSource
                .uploadFile("upload/1616940995641_tmpupload/IMG_001.jpg");
        log.info("Key is " + key);
    }
}