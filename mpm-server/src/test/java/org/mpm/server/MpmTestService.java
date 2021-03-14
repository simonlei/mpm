package org.mpm.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.pics.PicsDataSource;
import org.nutz.boot.test.NutzBootTest;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Lang;

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


    @Test
    public void testNull() {
        Integer v = null;
        boolean b1 = v == new Integer(-1);
        boolean b12 = Lang.equals(v, new Integer(-1));

        boolean b22 = Lang.equals(v, -1);
        assertThrows(NullPointerException.class, () -> {
            boolean b2 = v == -1;
        });
    }
}
