package org.mpm.client;

import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import org.mpm.client.util.ClientUtils;

public class LeftTabSet extends TabSet {

    public LeftTabSet() {
        super();
        addDatesTab();
        addFilesTab();
    }

    private void addFilesTab() {
        Tab filesTab = new Tab("按图片库查看");
        addTab(filesTab);
    }

    private void addDatesTab() {
        Tab datesTab = new Tab("按时间查看");
        TreeGrid datesGrid = new TreeGrid();
        datesGrid.setShowHeader(false);
        RestDataSource dataSource = ClientUtils.createDataSource("datesTree", "/pics/datesTree");

        datesGrid.setDataSource(dataSource);
        // datesGrid.setAutoFetchData(true);
        datesTab.setPane(datesGrid);
        addTab(datesTab);
    }
}
