package org.mpm.server.filesystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
@Slf4j
public class PhotoImporterDataSource {

    Map<String, PhotoImporter> tasks = new HashMap<>();

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

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/fileSystem/importProgress")
    @Ok("json")
    public NutMap getImportProgress(@Param("taskId") String taskId) {
        log.info("taskId " + taskId);
        PhotoImporter importer = tasks.get(taskId);
        if (importer != null) {
            if (importer.isFinished()) {
                tasks.remove(taskId);
            }
            return importer.getProgress();
        }
        return getFinishedProgress();
    }

    private NutMap getFinishedProgress() {
        return Lang.map("count", 100).setv("total", 100).setv("progress", 100);
    }

    private String importImages(String folder) {
        String taskId = UUID.randomUUID().toString();
        PhotoImporter importer = Mvcs.getIoc().get(PhotoImporter.class);
        tasks.put(taskId, importer);
        importer.init(taskId, folder).run();
        return taskId;
    }
}
