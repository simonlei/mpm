package org.mpm.server;

import org.junit.After;
import org.junit.Before;
import org.nutz.boot.NbApp;
import org.nutz.trans.Trans;

public class BaseTest {

    public static NbApp createNbApp() {
        return new MpmMainClass();
    }

    @Before
    public void setup() throws Exception {
        Trans.begin();
    }

    @After
    public void tearDown() throws Exception {
        Trans.rollback();
        // Trans.commit();
    }
}
