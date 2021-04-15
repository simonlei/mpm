package org.mpm.server.filesystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
                .uploadFile("upload/1616940995641_tmpupload/七上1025义工/IMG_004.jpg", "", null);
        // upload/1616940995641_tmpupload/IMG_001.jpg
        log.info("Key is " + key);
        key = photoImporterDataSource
                .uploadFile("upload/1616940995641_tmpupload/IMG_001.jpg", "", null);
        log.info("Key is " + key);
    }

    @Test
    public void testRegex() throws Exception {
        assertTrue(legal("user='simonlei'"));
        assertTrue(legal("abc=123"));
        assertTrue(legal("a in (1,2,3)"));
        assertFalse(legal("a inx (1,2,3)"));

        String[] split = "a, b, c".split(",");

        String[] strings = Stream.of(split).map(String::trim).toArray(String[]::new);
        Stream.of(strings).forEach(System.out::println);
    }

    private boolean legal(String c) {
        Pattern conditionPattern = Pattern.compile(".*(([\\>|\\<|\\=])|(in )).*");
        Matcher matcher = conditionPattern.matcher(c);
        return matcher.matches();
    }

    @Test
    public void testThread() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("done");
            }
        });
        t.start();
        Thread.sleep(1000);
        log.info(t.getState().toString());
        t.join();
        log.info("Join done.");
    }
}