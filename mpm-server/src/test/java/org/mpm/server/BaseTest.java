package org.mpm.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.nutz.trans.Trans;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BaseTest {

    @BeforeEach
    public void setup() throws Exception {
        Trans.begin();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Trans.rollback();
    }
}
