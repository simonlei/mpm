package org.mpm.server.pics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GeoCheckerTest {

    @Autowired
    GeoChecker geoChecker;

    @Test
    void testRemoveDuplicate() {
        assertEquals("", geoChecker.removeDuplicate(null));
        assertEquals("", geoChecker.removeDuplicate(""));
        assertEquals("abc", geoChecker.removeDuplicate("abc"));
        assertEquals("abc\ndef", geoChecker.removeDuplicate("abc\ndef"));
        assertEquals("abc\ndef", geoChecker.removeDuplicate("abc\ndef\ndef"));
        assertEquals("abc\ndef", geoChecker.removeDuplicate("abc\nabc\n def"));
        assertEquals("abc\ndef", geoChecker.removeDuplicate("abc\nabc\ndef\ndef"));
        assertEquals("abc\ndef", geoChecker.removeDuplicate("abc\n\n def\n \nabc \ndef"));
        assertEquals("abc\ndef\nghi", geoChecker.removeDuplicate("abc \ndef\n\n \n\n abc\n def\n def \nghi \nghi \n"));
    }
}