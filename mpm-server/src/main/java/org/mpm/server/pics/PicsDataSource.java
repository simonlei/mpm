package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.metas.DataSourceResponse;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
public class PicsDataSource {

    @Inject
    PicsModule picsModule;


    @At("/pics/fetch")
    @Ok("json")
    public NutMap fetchPics(@Param("_startRow") int startRow, @Param("_endRow") int endRow) {
        int count = picsModule.count();
        List<EntityPhoto> photos = picsModule.query(startRow, endRow);

        DataSourceResponse resp = new DataSourceResponse(0, startRow, startRow + photos.size(),
                count, photos);
        return resp.wrapResult();
    }
}
