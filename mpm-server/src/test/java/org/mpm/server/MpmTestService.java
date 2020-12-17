package org.mpm.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpm.server.pics.PicsDataSource;
import org.nutz.boot.test.NutzBootTest;
import org.nutz.ioc.loader.annotation.Inject;

@NutzBootTest
@Slf4j
public class MpmTestService {

    @Inject
    PicsDataSource picsDataSource;

    @Test
    public void testNothing() throws UnsupportedEncodingException {
        Integer value = picsDataSource.getOldId(
                "_operationType=remove&_textMatchStyle=exact&_oldValues=%7B%22id%22%3A96%2C%22name%22%3A%22p7ijpt2vjujdlocmk8ie3bkgqb%22%7D&_componentId=isc_PicsGrid_0&_dataSource=pics&isc_metaDataPrefix=_&isc_dataFormat=json");
        assertEquals(value, 96);

    }
}
