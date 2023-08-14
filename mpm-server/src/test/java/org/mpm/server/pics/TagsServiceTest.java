package org.mpm.server.pics;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class TagsServiceTest {

    @Autowired
    private TagsService tagsService;

    @Test
    void getAllTags() {
        List<String> allTags = tagsService.getAllTags();
        log.info("AllTags: " + allTags);
    }
}