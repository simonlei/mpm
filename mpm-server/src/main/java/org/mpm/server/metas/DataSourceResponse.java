package org.mpm.server.metas;

import java.util.List;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

public class DataSourceResponse {

    int status;
    int startRow;
    int endRow;
    int totalRows;
    List data;

    public DataSourceResponse() {
    }

    public DataSourceResponse(int startRow, int endRow, int totalRows, List data) {
        this.startRow = startRow;
        this.endRow = endRow;
        this.totalRows = totalRows;
        this.data = data;
    }

    public static NutMap wrapData(List data) {
        DataSourceResponse resp = new DataSourceResponse(0, data.size(), data.size(), data);
        return resp.wrapResult();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public NutMap wrapResult() {
        return Lang.map("response", this);
    }
}
