package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.metas.DataSourceResponse;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
public class PicsDataSource {

    @Inject
    PicsModule picsModule;


    @At("/pics/fetch")
    @Ok("json")
    public NutMap fetchPics(@Param("_startRow") int startRow, @Param("_endRow") int endRow,
            @Param("trashed") boolean trashed) {
        int count = picsModule.count(trashed);
        List<EntityPhoto> photos = picsModule.query(startRow, endRow, trashed);

        DataSourceResponse resp = new DataSourceResponse(0, startRow, startRow + photos.size(),
                count, photos, Lang.map("a", 123));

        return resp.wrapResult();
    }

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/count")
    @Ok("json")
    public int getPhotosCount(@Param("trashed") boolean trashed) {
        return picsModule.count(trashed);
    }

}
