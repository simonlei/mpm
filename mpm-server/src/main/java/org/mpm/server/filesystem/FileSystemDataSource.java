package org.mpm.server.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.mpm.server.metas.DataSourceResponse;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
public class FileSystemDataSource {

    @At("fileSystem/fetch")
    @Ok("json")
    public NutMap fetch(@Param("parent") String parent) {
        if (Lang.equals("null", parent)) {
            parent = null;
        }
        System.out.println("parentId " + parent + " isNull: " + Strings.isBlank(parent));
        File[] files = parent == null ? File.listRoots() : new File(parent).listFiles();

        List data = new ArrayList();
        for (File f : files) {
            if (!f.getName().startsWith(".")) {
                data.add(Lang.map("path", f.getPath()).setv("name", f.getName())
                        .setv("parent", f.getParent()));
            }
        }
        return DataSourceResponse.wrapData(data);
    }
}
