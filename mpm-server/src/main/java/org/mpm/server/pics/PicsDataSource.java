package org.mpm.server.pics;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.entity.EntityPhoto;
import org.mpm.server.metas.DataSourceResponse;
import org.mpm.server.metas.ModifyResponse;
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

    private List<NutMap> switchTrash(List ids) {
        log.info("to trash " + ids);
        Sql sql = Sqls.create("update t_photos set trashed = !trashed where id in (@ids)");
        sql.setParam("ids", ids);
        dao.execute(sql);
        List<NutMap> result = new ArrayList<>();
        for (Object o : ids) {
            result.add(Lang.map("id", o));
        }
        return result;
    }


    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/remove")
    @Ok("json")
    public Object removePics(@Param("..") Reader reader, HttpServletRequest request)
            throws UnsupportedEncodingException {
        String oldValues = request.getParameter("_oldValues");
        String req = Streams.readAndClose(reader);
        if (oldValues == null) { // 删除单条记录
            Integer v = getOldId(req);

            return ModifyResponse.makeResponse("remove", switchTrash(Lang.list(v)).get(0));
        } else { // 多条
            log.info("req:" + req);
            List list = (List) Mapl.cell(Json.fromJson(req), "transaction.operations");
            List<Integer> ids = new ArrayList<>();
            for (Object o : list) {
                ids.add((Integer) Mapl.cell(o, "id"));
            }
            log.info("ids:" + ids);
            List<NutMap> data = switchTrash(ids);
            List<NutMap> result = new ArrayList<>();
            for (NutMap d : data) {
                result.add(ModifyResponse.makeResponse("remove", d));
            }
            return result;
        }
    }

    public Integer getOldId(String req) throws UnsupportedEncodingException {
        Pattern pattern = Regex.getPattern("id=([^\\&]*)\\&");
        Matcher matcher = pattern.matcher(req);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    @AdaptBy(type = WhaleAdaptor.class)
    @At("/pics/count")
    @Ok("json")
    public int getPhotosCount(@Param("trashed") boolean trashed) {
        return picsModule.count(trashed);
    }

}
