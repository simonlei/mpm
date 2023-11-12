package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mpm.server.BaseTest;
import org.nutz.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
@Slf4j
public class PhotoImportServiceTest extends BaseTest {

    @Autowired
    PhotoImportService photoImportService;
    @Autowired
    Dao dao;

    @MockBean
    PicsService picsService;

    @Test
    public void testImportPhoto() throws FileNotFoundException {
        Mockito.when(picsService.saveCosFile(Mockito.any(), Mockito.any()))
                .thenAnswer((Answer<String>) invocation -> "image/jpeg");
        String key = photoImportService.uploadFile("upload/1616940995641_tmpupload/七上1025义工/IMG_004.jpg",
                new Date(), null, null);
        // upload/1616940995641_tmpupload/IMG_001.jpg
        log.info("Key is " + key);
        key = photoImportService.uploadFile("upload/1616940995641_tmpupload/IMG_001.jpg", new Date(), null, null);
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