package org.mpm.server.pics;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.metas.DataSourceResponse;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.mapl.Mapl;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@IocBean
@Slf4j
public class PicsDataSource {

    @Inject
    PicsModule picsModule;
    @Inject
    Dao dao;


    @At("/pics/fetch")
    @Ok("json")
    public NutMap fetchPics(@Param("_startRow") int startRow, @Param("_endRow") int endRow,
            @Param("trashed") boolean trashed) {
        int count = picsModule.count(trashed);
        List<EntityPhoto> photos = picsModule.query(startRow, endRow, trashed);

        DataSourceResponse resp = new DataSourceResponse(0, startRow, startRow + photos.size(),
                count, photos);

        return resp.wrapResult();
    }

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/remove")
    @Ok("json")
    public NutMap removePics(@Param("..") Reader reader, HttpServletRequest request)
            throws UnsupportedEncodingException {
        String oldValues = request.getParameter("_oldValues");
        String req = Streams.readAndClose(reader);
        if (oldValues == null) { // 删除单条记录
            Integer v = getOldId(req);
            log.info("old value:" + v + " and id: " + v);
            Sql sql = Sqls.create("update t_photos set trashed = !trashed where id in (@ids)");
            sql.setParam("ids", Lang.list(v));
            dao.execute(sql);

            return DataSourceResponse.wrapData(Lang.list(Lang.map("id", v)));
        } else { // 多条

        }
        // log.info("req " + Dumps.obj(req));
        log.info("request: " + request);
        log.info("oldvalues: " + oldValues);

        //log.info("old values:" + oldValues);
        log.info("req:" + req);
        return Lang.map("1", 2);
    }

    public Integer getOldId(String req) throws UnsupportedEncodingException {
        Pattern pattern = Regex.getPattern("_oldValues=([^\\&]*)\\&");
        Matcher matcher = pattern.matcher(req);
        matcher.find();
        String v = URLDecoder.decode(matcher.group(1), "UTF-8");
        return (Integer) Mapl.cell(Json.fromJson(v), "id");
    }

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/count")
    @Ok("json")
    public int getPhotosCount(@Param("trashed") boolean trashed) {
        return picsModule.count(trashed);
    }

}
