package org.mpm.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.nutz.lang.Lang;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyUtilsTest {

    @Test
    void testCell() {
        assertEquals("", MyUtils.cell(null, null));
        assertEquals("", MyUtils.cell(null, "nothing"));
        assertEquals("ccc", MyUtils.cell(Lang.map("aaa", Lang.map("bbb", "ccc")), "aaa.bbb"));
        assertEquals("", MyUtils.cell(Lang.map("aaa", Lang.map("bbb", "ccc")), "aaa.notexist"));
        assertEquals("", MyUtils.cell(Lang.map("aaa", Lang.map("bbb", "ccc")), "notexist.notexist"));
    }

    @Test
    void testDate() {
        assertNull(MyUtils.parseDate(null, "nothing"));
        assertNull(MyUtils.parseDate("2020:01:02 03:04:05", "wrong format"));
        Date d = MyUtils.parseDate("2020:01:02 03:04:05", "yyyy:MM:dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        assertEquals(2020, calendar.get(Calendar.YEAR));
        assertEquals(0, calendar.get(Calendar.MONTH));
        assertEquals(2, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(3, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(4, calendar.get(Calendar.MINUTE));
        assertEquals(5, calendar.get(Calendar.SECOND));
    }

    @Test
    void testParseInt() {
        assertEquals(1, MyUtils.parseInt(null, 1));
        assertEquals(0, MyUtils.parseInt("xxx", 0));
        assertEquals(888, MyUtils.parseInt("888", 0));
    }
}