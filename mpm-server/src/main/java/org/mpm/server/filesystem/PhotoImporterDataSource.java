package org.mpm.server.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.pics.PicsModule;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PhotoImporterDataSource {

    @Autowired
    Dao dao;
    @Autowired
    PicsModule picsModule;

    // used in client
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestBody UploadFileSchema upload) {
        String key = upload.key;
        log.info("Key is " + key);
        if (Lang.isNotEmpty(upload.err)) {
            log.info("Error {} when upload {}, with data {} ", upload.err, key, upload.data);
        }
        // upload/1616851720630_tmpupload/七上1025义工/IMG_002.jpg
        String[] paths = key.split("\\/");
        String path = "/" + paths[1];
        // name: 1616851720630_tmpupload 去掉前面的 1616851720630_
        EntityFile parent = existOrCreate(null, path, paths[1].substring(14), true);
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

    private synchronized EntityFile existOrCreate(EntityFile parent, String path, String name,
            boolean isFolder) {
        EntityFile file = dao.fetch(EntityFile.class, Cnd.where("path", "=", path));
        if (file != null) {
            return file;
        }
        file = EntityFile.builder().path(path).name(name).isFolder(isFolder)
                .parentId(parent == null ? null : parent.getId())
                .build();
        dao.insert(file, true, false, false);
        return file;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class UploadFileSchema {

        String key;
        String err;
        Object data;
    }
}
