package org.mpm.server.util;

import static java.lang.String.format;
import static java.time.LocalDate.now;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.Exps;
import org.nutz.lang.util.NutMap;

@Slf4j
public class DaoUtil {

    /**
     * 返回指定字段 list
     *
     * @param dao
     * @param sql
     * @param col
     */
    public static List fetchList(Dao dao, String sql, String col) {
        Sql s = Sqls.create(sql);
        s.setCallback((conn, rs, sql1) -> {
            List result = new ArrayList();
            while (rs.next()) {
                result.add(rs.getObject(col));
            }
            return result;
        });
        dao.execute(s);
        return s.getList(Object.class);
    }

    public static List fetchList(Dao dao, Sql s, String col) {
        s.setCallback((conn, rs, sql) -> {
            List result = new ArrayList();
            while (rs.next()) {
                result.add(rs.getObject(col));
            }
            return result;
        });
        dao.execute(s);
        return s.getList(Object.class);
    }

    public static List<NutMap> fetchMaps(Dao dao, String sql) {
        Sql s = Sqls.create(sql);
        s.setCallback(Sqls.callback.maps());
        dao.execute(s);
        return s.getList(NutMap.class);
    }

    public static List<Record> fetchRecords(Dao dao, Sql s) {
        s.setCallback(Sqls.callback.records());
        dao.execute(s);
        return s.getList(Record.class);
    }

    public static List<NutMap> fetchMaps(Dao dao, Sql s) {
        s.setCallback(Sqls.callback.maps());
        dao.execute(s);
        return s.getList(NutMap.class);
    }

    public static <T> T fetchEntity(Dao dao, Sql s, Class<T> clazz) {
        s.setCallback(Sqls.callback.entities());
        s.setEntity(dao.getEntity(clazz));
        dao.execute(s);
        return s.getObject(clazz);
    }

    public static List fetchEntities(Dao dao, Sql s, Class clazz) {
        s.setCallback(Sqls.callback.entities());
        s.setEntity(dao.getEntity(clazz));
        dao.execute(s);
        return s.getList(clazz);
    }

    public static NutMap fetchMap(Dao dao, String sql) {
        Sql s = Sqls.create(sql);
        s.setCallback(Sqls.callback.map());
        dao.execute(s);
        return s.getObject(NutMap.class);
    }

    public static NutMap fetchMap(Dao dao, Sql s) {
        s.setCallback(Sqls.callback.map());
        dao.execute(s);
        return s.getObject(NutMap.class);
    }


    public static Object fetchOne(Dao dao, String sql, String colName) {
        Sql s = Sqls.create(sql);
        s.setCallback(Sqls.callback.map());
        dao.execute(s);
        NutMap m = s.getObject(NutMap.class);
        if (m == null) {
            return null;
        }
        return m.get(colName);
    }

    public static Object fetchOne(Dao dao, Sql s, String colName) {
        s.setCallback(Sqls.callback.map());
        dao.execute(s);
        NutMap m = s.getObject(NutMap.class);
        if (m == null) {
            return null;
        }
        return m.get(colName);
    }


    /**
     * 根据map的值进行查询，返回一条记录
     *
     * @param tableName
     * @param m
     * @return
     */
    public static Record fetchByMap(Dao dao, String tableName, Map m) {
        Cnd cnd = Cnd.NEW();
        for (Object key : m.keySet()) {
            cnd.and(Exps.eq((String) key, m.get(key)));
        }
        return dao.fetch(tableName, cnd);
    }

    public static void createTmpBeforeStart(Dao dao, String t) {
        dao.execute(Sqls.create(format("drop table if exists %s_tmp", t)));
        dao.execute(Sqls.create(format("create table %s_tmp like %s", t, t)));
    }

    public static void markTableByDateAfterFinish(Dao dao, String t) {
        String today = now().toString().replaceAll("-", "_");
        Sql sql1 = Sqls.create(format("drop table if exists %s_%s", t, today));
        dao.execute(sql1);
        Sql sql = Sqls.create(format("rename table %s to %s_%s", t, t, today));
        dao.execute(sql);
        Sql sql2 = Sqls.create(format("rename table %s_tmp to %s", t, t));
        dao.execute(sql2);
    }


    /**
     * 删除一个周前的历史备份表
     */
    public static void dropLongHistoryTables(Dao dao, String t) {
        List<String> tables = DaoUtil.fetchList(dao, format("select distinct table_name\n"
                        + "     from information_schema.TABLES\n"
                        + "      where table_schema='techmap' and TABLE_NAME like '%s_%%' order by table_name", t),
                "table_name");
        if (tables == null) {
            return;
        }
        log.info("all tables:  " + tables.size());
        String date = LocalDate.now().plusWeeks(-1).toString().replaceAll("-", "_");

        for (String table : tables) {
            if (table.compareTo(t + "_" + date) <= 0) {
                Sql sql1 = Sqls.create(format("drop table if exists %s", table));
                dao.execute(sql1);
                log.info("deleted  table {}", table);
            } else {
                break;
            }
        }
    }
}
