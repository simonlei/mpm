package org.mpm.server.metas;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

@Data
@Builder
public class ModifyResponse {

    int affectedRows = 1;
    Map data;
    boolean invalidateCache = false;
    boolean isDSResponse = true;
    String operationType;
    int queueStatus = 0;
    int status = 0;

    public static NutMap makeResponse(String operationType, Map data) {
        return Lang.map("response",
                ModifyResponse.builder().operationType(operationType).isDSResponse(true).data(data)
                        .affectedRows(1).build());
    }

}
