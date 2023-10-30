package org.mpm.server;

import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.nutz.lang.Lang;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class MpmTestService {

    @Test
    public void testNull() {
        Integer v = null;
        boolean b1 = v == Integer.valueOf(-1);
        boolean b12 = Lang.equals(v, Integer.valueOf(-1));

        boolean b22 = Lang.equals(v, -1);
        assertThrows(NullPointerException.class, () -> {
            boolean b2 = v == -1;
        });
    }
}
