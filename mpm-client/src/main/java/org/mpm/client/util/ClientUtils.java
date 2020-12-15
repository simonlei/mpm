package org.mpm.client.util;

import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.util.JSOHelper;
import com.smartgwt.client.util.JSON;
import java.util.HashMap;
import java.util.Map;

public class ClientUtils {

    public static RestDataSource createDataSource(String id, String fetchUrl) {
        RestDataSource dataSource = new RestDataSource();
        dataSource.setID(id);
        dataSource.setDataFormat(DSDataFormat.JSON);
        dataSource.setFetchDataURL(fetchUrl);
        dataSource.setJsonPrefix("");
        dataSource.setJsonSuffix("");

        return dataSource;
    }

    public static RPCRequest makeRPCRequest(String url) {
        RPCRequest req = new RPCRequest();
        req.setUseSimpleHttp(true);
        req.setActionURL(url);
        return req;
    }

    public static RPCRequest makeRPCRequest(String url, String key, Object value) {
        RPCRequest req = makeRPCRequest(url);
        HashMap data = new HashMap();
        data.put(key, value);
        req.setParams(data);
        return req;
    }

    public static Map getResponseAsMap(RPCResponse rpcResponse) {
        return JSOHelper.convertToMap(JSON.decode(rpcResponse.getDataAsString()));
    }
}
