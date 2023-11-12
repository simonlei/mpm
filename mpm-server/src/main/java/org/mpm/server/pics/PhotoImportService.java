package org.mpm.server.pics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityMeta;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class PhotoImportService {

    @Autowired
    Dao dao;
    @Autowired
    PicsService picsService;


    @PostMapping("/api/uploadPhoto")
    public int uploadPhoto(@RequestParam("file") MultipartFile file,
            @RequestParam("lastModified") String lastModified,
            @RequestParam("batchId") String batchId) throws IOException {
        Date date = new Date(Long.valueOf(lastModified));
        log.info("total files {} with {}", file.getSize(), date);
        String key = "upload/" + batchId + "_" + file.getOriginalFilename();
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("tmp", "" + Math.random());
            Streams.copy(file.getInputStream(), new FileOutputStream(tmpFile), true);
            String contentType = file.getContentType();
            picsService.uploadFile(key, contentType, tmpFile);
            uploadFile(key, date, contentType, tmpFile);
        } catch (Exception e) {
            log.error("Can't upload file:" + key, e);
            dao.insert(EntityMeta.builder().key("Error_Log_" + key)
                    .value(Strings.cutStr(2000, e.getMessage(), "..."))
                    .build());
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
        return 0;
    }

    public String uploadFile(String key, Date date, String contentType, File tmpFile) {
        log.info("Key is " + key);
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
        EntityPhoto photo = picsService.savePhotoInDb(key, name, date.getTime(), contentType, tmpFile);
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

        long date;
        String key;
        String err;
        Object data;
    }
}
