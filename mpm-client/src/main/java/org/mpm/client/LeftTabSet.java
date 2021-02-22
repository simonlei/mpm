package org.mpm.client;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.NodeClickEvent;
import com.smartgwt.client.widgets.tree.events.NodeClickHandler;

public class LeftTabSet extends TabSet {

    public static LeftTabSet instance;
    private TreeGrid datesGrid;
    private Criteria criteria;

    public LeftTabSet() {
        super();
        addDatesTab();
        addFilesTab();
        instance = this;
    }

    public static Criteria getCriteria() {
        Criteria criteria = instance.criteria;
        if (criteria == null) {
            criteria = new Criteria();
        }
        return criteria;
    }

    private void addFilesTab() {
        Tab filesTab = new Tab("按图片库查看");
        addTab(filesTab);
    }

    private void addDatesTab() {
        Tab datesTab = new Tab("按时间查看");
        datesGrid = new TreeGrid();
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
                TreeNode node = nodeClickEvent.getNode();
                String title = node.getTitle();
                criteria = new Criteria();
                if (title.contains("年")) {
                    criteria.addCriteria("theYear", title.substring(0, title.length() - 1));
                } else {
                    criteria.addCriteria("theYear", node.getAttributeAsInt("year"));
                    criteria.addCriteria("theMonth", node.getAttributeAsInt("month"));
                }
                PicsGrid.reloadData();
                SC.logWarn("Click:" + node.getTitle());
            }
        });
        datesTab.setPane(datesGrid);
        addTab(datesTab);
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        datesGrid.fetchData(criteria);
    }

    public void reloadData(boolean trashed) {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", trashed);
        TreeNode selectedRecord = datesGrid.getSelectedRecord();
        datesGrid.invalidateCache();
        datesGrid.fetchData(criteria);
        datesGrid.selectRecord(selectedRecord);
    }
}
