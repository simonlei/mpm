package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.metas.DataSourceResponse;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

@IocBean
public class PicsDataSource {

    @Inject
    PicsModule picsModule;


    @At("/pics/fetch")
    @Ok("json")
    public NutMap fetchPics() {
        int count = picsModule.count();
        List<EntityPhoto> photos = picsModule.query();

        DataSourceResponse resp = new DataSourceResponse(0, 0, photos.size(), count, photos);
        return resp.wrapResult();
    }
}
