package org.mpm.server.pics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CosProxyTest {
    @Autowired
    CosProxy cosProxy;

    @Test
    void testSubParam() {
        String key = "/small/rpdsoktgq4hg3qd80q9ag0931u/thumb180";
        String param = cosProxy.subParam(key);
        assertEquals("thumb180", param);
        assertEquals("/small/rpdsoktgq4hg3qd80q9ag0931u", key.substring(0, key.length() - param.length() - 1));
        assertEquals("thumb", cosProxy.subParam("/small/rpdsoktgq4hg3qd80q9ag0931u/thumb"));
    }
}