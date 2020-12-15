package org.mpm.server.metas;

import java.util.List;
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
    List data;
    NutMap customData;

    public static NutMap wrapData(List data) {
        DataSourceResponse resp = new DataSourceResponse(0, 0, data.size(), data.size(), data,
                new NutMap());
        return resp.wrapResult();
    }

    public NutMap wrapResult() {
        return Lang.map("response", this);
    }
}
