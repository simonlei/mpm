package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GisServiceTest {

    @Autowired
    GisService gisService;

    @Test
    void testGetAddress() {
        assertEquals("广东省深圳市南山区文苑路", gisService.getAddress(22.5776500000, 113.9504277778));
    }
}