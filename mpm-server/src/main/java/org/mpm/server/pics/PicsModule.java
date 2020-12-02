package org.mpm.server.pics;

import java.util.List;
import org.mpm.server.entity.EntityPhoto;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public class PicsModule {

    @Inject
    Dao dao;

    public int count() {
        return dao.count(EntityPhoto.class);
    }

    public List<EntityPhoto> query() {
        return dao.query(EntityPhoto.class, null);
    }
}
