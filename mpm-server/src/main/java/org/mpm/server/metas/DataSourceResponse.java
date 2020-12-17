package org.mpm.server.metas;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceResponse {

    int status;
    int startRow;
    int endRow;
    int totalRows;
    Object data;

    public static NutMap wrapData(List data) {
        DataSourceResponse resp = new DataSourceResponse(0, 0, data.size(), data.size(), data);
        return resp.wrapResult();
    }

    public static NutMap wrapData(Map data) {
        DataSourceResponse resp = new DataSourceResponse(0, 0, 1, 1, data);
        return resp.wrapResult();
    }

    public NutMap wrapResult() {
        return Lang.map("response", this);
    }
}
