package org.mpm.server.pics;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.view.ServerRedirectView;

@IocBean
@Slf4j
public class ImageController {

    @Inject
    PropertiesProxy conf;
    @Inject
    Dao dao;

    @At("/thumb/?")
    @Ok("raw")
    public View thumb(String name) {
        return new ServerRedirectView(String.format("https://%s.cos.%s.myqcloud.com/%s/thumb",
                conf.get("cos.bucket"), conf.get("cos.region"), name));
    }

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/switchTrash")
    @Ok("json")
    public NutMap switchTrash(@Param("ids") List ids) {
        log.info("to trash " + ids);
        Sql sql = Sqls.create("update t_photos set trashed = !trashed where id in (@ids)");
        sql.setParam("ids", ids);
        dao.execute(sql);
        return Lang.map("success", true);
    }
}
