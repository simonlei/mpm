package org.mpm.client.util;

import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.types.DSDataFormat;

public class Utils {

    public static RestDataSource createDataSource(String id, String fetchUrl) {
        RestDataSource dataSource = new RestDataSource();
        dataSource.setID(id);
        dataSource.setDataFormat(DSDataFormat.JSON);
        dataSource.setFetchDataURL(fetchUrl);
        dataSource.setJsonPrefix("");
        dataSource.setJsonSuffix("");

        return dataSource;
    }
}
