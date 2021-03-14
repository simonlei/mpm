package org.mpm.server.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.mpm.server.pics.ProgressDataSource;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
@Slf4j
public class PhotoImporterDataSource {


    @AdaptBy(type = WhaleAdaptor.class)
    @At("/fileSystem/importImages")
    @Ok("raw")
    public String importImagesApi(@Param("folder") String folder) {
        // String folder = (String) map.get("folder");
        if (Strings.isBlank(folder)) {
            throw Lang.makeThrow("No folder selected");
        }

        log.info("Folder: " + folder);
        return importImages(folder);
    }

    private String importImages(String folder) {
        PhotoImporter importer = Mvcs.getIoc().get(PhotoImporter.class);
        String taskId = ProgressDataSource.addTask(importer);
        new Thread(importer.init(folder)).start();
        return taskId;
    }
}
