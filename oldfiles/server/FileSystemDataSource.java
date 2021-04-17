package org.mpm.server.filesystem;

import com.isomorphic.datasource.DSRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

@IocBean
@Slf4j
public class FileSystemDataSource {

    public List fetch(DSRequest req) {
        String parent = (String) req.getCriteria().get("parent");
        if (Lang.equals("null", parent)) {
            parent = null;
        }
        System.out.println("parentId " + parent + " isNull: " + Strings.isBlank(parent));
        File[] files = parent == null ? File.listRoots() : new File(parent).listFiles();
        if (files == null) {
            files = new File[1];
            files[0] = new File(System.getProperty("user.home"));
        }
        List data = new ArrayList();
        for (File f : files) {
            log.info("File is " + f.getPath());
            if (!f.getName().startsWith(".") && f.isDirectory()) {
                data.add(Lang.map("path", f.getPath()).setv("name", f.getName())
                        .setv("parent", f.getParent()));
            }
        }
        return data;
    }
}
