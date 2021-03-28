package org.mpm.server.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.pics.PicsModule;
import org.mpm.server.pics.ProgressDataSource;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
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

    @Inject
    Dao dao;
    @Inject
    PicsModule picsModule;

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/uploadFile")
    @Ok("json")
    public String uploadFile(@Param("key") String key) {
        log.info("Key is " + key);
        // upload/1616851720630_tmpupload/七上1025义工/IMG_002.jpg
        String[] paths = key.split("\\/");
        String path = "/" + paths[1];
        // name: 1616851720630_tmpupload 去掉前面的 1616851720630_
        EntityFile parent = existOrCreate(null, path, paths[1].substring(14), true);
        parent.setRootId(parent.getId());
        // skip upload, 1616851720630_tmpupload and last one
        if (paths.length > 2) {
            for (int i = 2; i < paths.length - 1; i++) {
                path += "/" + paths[i];
                // path exist?
                parent = existOrCreate(parent, path, paths[i], true);
            }
        }
        String name = paths[paths.length - 1];
        EntityPhoto photo = picsModule.savePhotoInDb(parent, key, name);
        if (photo != null) {
            EntityFile file = existOrCreate(parent, path + "/" + name, name, false);
            file.setPhotoId(photo.getId());
            file.setPhotoName(photo.getName());
            dao.update(file);
            return "" + file.getId();
        }

        return "";
    }

    private EntityFile existOrCreate(EntityFile parent, String path, String name,
            boolean isFolder) {
        EntityFile file = dao.fetch(EntityFile.class, Cnd.where("path", "=", path));
        if (file != null) {
            return file;
        }
        file = EntityFile.builder().path(path).name(name).isFolder(isFolder)
                .parentId(parent == null ? null : parent.getId())
                .rootId(parent == null ? null : parent.getRootId())
                .build();
        dao.insert(file, true, false, false);
        return file;
    }

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
