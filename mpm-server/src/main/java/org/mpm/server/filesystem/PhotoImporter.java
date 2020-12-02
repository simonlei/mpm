package org.mpm.server.filesystem;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

@IocBean(singleton = false)
public class PhotoImporter implements Runnable {

    String taskId;
    String folder;

    @Override
    public void run() {

    }

    public PhotoImporter init(String taskId, String folder) {
        this.taskId = taskId;
        this.folder = folder;
        return this;
    }

    public boolean isFinished() {
        return true;
    }

    public NutMap getProgress() {
        return Lang.map("count", 100).setv("total", 100).setv("progress", 100);
    }
}
