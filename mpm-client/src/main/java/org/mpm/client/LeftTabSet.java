package org.mpm.client;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.events.NodeClickEvent;
import com.smartgwt.client.widgets.tree.events.NodeClickHandler;

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
        DataSource dataSource = DataSource.get("datesTree");

        datesGrid.setFields(new TreeGridField("title"));
        datesGrid.setDataSource(dataSource);
        datesGrid.setAutoFetchData(false);
        // datesGrid.setCanEdit(false);
        datesGrid.setLoadDataOnDemand(false);
        datesGrid.addNodeClickHandler(new NodeClickHandler() {
            @Override
            public void onNodeClick(NodeClickEvent nodeClickEvent) {

                SC.logWarn("Click:" + nodeClickEvent.getNode().getTitle());
            }
        });
        datesTab.setPane(datesGrid);
        addTab(datesTab);
        datesGrid.fetchData();
    }
}
