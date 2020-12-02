package org.mpm.server.filesystem;

import java.util.Map;
import java.util.UUID;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
public class PhotoImporter {

    private final static Log log = Logs.get();

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/fileSystem/importImages")
    @Ok("raw")
    public String importImagesApi(@Param("..") Map map) {
        String folder = (String) map.get("folder");
        if (Strings.isBlank(folder)) {
            throw Lang.makeThrow("No folder selected");
        }

        log.info("Folder: " + folder);
        return importImages(folder);
    }

    private String importImages(String folder) {
        return UUID.randomUUID().toString();
    }
}
