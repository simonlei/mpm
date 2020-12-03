package org.mpm.server.filesystem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityFile;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.pics.PicsModule;
import org.mpm.server.util.MyUtils;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

@IocBean(singleton = false)
@Slf4j
public class PhotoImporter implements Runnable {

    String taskId;
    String folder;
    int total = -1;
    int count = 0;
    int picsCount = 0;

    @Inject
    PicsModule picsModule;
    @Inject
    Dao dao;

    @Override
    public void run() {
        File root = new File(folder);
        total = collectFilesInPath(new File[]{root});
        EntityFile rootObj = new EntityFile();
        rootObj.setName(root.getName());
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY年MM月dd日");
        rootObj.setDescription("导入日期：" + formatter.format(new Date()));
        rootObj.setIsFolder(true);
        dao.insert(rootObj);
        scanFilesIn(root, rootObj, rootObj);
    }

    private void scanFilesIn(File dir, EntityFile rootObj, EntityFile parentObj) {
        File[] subFiles = dir.listFiles();
        Arrays.sort(subFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String n1 = o1.getName();
                String n2 = o2.getName();
                Long l1 = MyUtils.getPrefixLong(n1);
                Long l2 = MyUtils.getPrefixLong(n2);
                if ((l1.equals(-1l) && l2.equals(-1l)) || l1 - l2 == 0) {
                    return n1.compareTo(n2); // Both don't have prefix number;
                }
                return l1.compareTo(l2);
            }
        });
        log.info("files after : " + subFiles.length);
        for (File f : subFiles) {
            if (f.isDirectory()) {
                EntityFile subObj = new EntityFile();
                subObj.setName(f.getName());
                subObj.setIsFolder(true);
                subObj.setParentId(parentObj.getId());
                subObj.setRootId(rootObj.getId());
                dao.insert(subObj);
                scanFilesIn(f, rootObj, subObj);
            } else {
                log.info("Scanning " + f.getAbsolutePath());
                count++;
                try {
                    EntityPhoto saved = picsModule.saveFileInRepository(f, null);
                    if (saved != null) {
                        EntityFile fileObj = new EntityFile();
                        fileObj.setRootId(rootObj.getId());
                        fileObj.setParentId(parentObj.getId());
                        fileObj.setName(f.getName());
                        fileObj.setIsFolder(false);
                        fileObj.setPhotoId(saved.getId());
                        fileObj.setPhotoName(saved.getName());
                        dao.insert(fileObj);
                        picsCount++;
                        // f.delete();
                    }
                } catch (Throwable e) {
                    log.error("扫描图片文件时出错。", e);
                }
            }
        }

    }

    protected int collectFilesInPath(File[] files) {
        int totalFiles = 0;
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                totalFiles += collectFilesInPath(file.listFiles());
            } else {
                totalFiles++;
            }
        }

        return totalFiles;
    }

    public PhotoImporter init(String taskId, String folder) {
        this.taskId = taskId;
        this.folder = folder;
        return this;
    }

    public boolean isFinished() {
        return calcProgress() >= 100;
    }

    public NutMap getProgress() {
        return Lang.map("count", count).setv("total", total)
                .setv("picsCount", picsCount).setv("progress", calcProgress());
    }

    private int calcProgress() {
        if (total == -1) {
            return -1; // not start
        } else if (total == 0) {
            return 100;
        }
        return count * 100 / total;
    }
}
