package org.mpm.server.pics;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.pics.DateController.PicDateRequest;
import org.mpm.server.pics.DateController.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DateControllerTest {

    @Autowired
    DateController dateController;

    @Test
    void getPicsDate() {
        List<TreeNode> picsDate = dateController.getPicsDate(PicDateRequest.builder().build());
        log.info("pics date {}", picsDate);
    }
}