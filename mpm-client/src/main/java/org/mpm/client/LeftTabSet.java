package org.mpm.client;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class LeftTabSet extends TabSet {

    public static LeftTabSet instance;
    private TreeGrid datesGrid;
    private TreeGrid filesGrid;
    private Criteria criteria;
    private int selectedTab = 0;

    public LeftTabSet() {
        super();
        addDatesTab();
        addFilesTab();
        addTabSelectedHandler(tabSelectedEvent -> {
            int oldSelected = selectedTab;
            selectedTab = tabSelectedEvent.getTabNum();
            if (oldSelected != selectedTab) {
                reloadData();
            }
        });
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
        filesGrid = new TreeGrid();
        filesGrid.setShowHeader(false);
        filesGrid.setShowRoot(false);
        DataSource dataSource = DataSource.get("filesTree");
        filesGrid.setFields(new TreeGridField("name"));
        filesGrid.setDataSource(dataSource);
        filesGrid.setAutoFetchData(false);
        filesGrid.addNodeClickHandler(nodeClickEvent -> {
            TreeNode node = nodeClickEvent.getNode();
            criteria = new Criteria();
            criteria.addCriteria("fileId", node.getAttributeAsInt("id"));
            PicsGrid.reloadData();
            SC.logWarn("reload fileid");
        });
        filesTab.setPane(filesGrid);
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
        datesGrid.setLoadDataOnDemand(false);
        datesGrid.addNodeClickHandler(nodeClickEvent -> {
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
        });
        datesTab.setPane(datesGrid);
        addTab(datesTab);
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        datesGrid.fetchData(criteria);
    }

    public void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", PicsGrid.isTrashed());
        TreeGrid grid = selectedTab == 0 ? datesGrid : filesGrid;
        TreeNode selectedRecord = grid.getSelectedRecord();
        grid.invalidateCache();
        grid.fetchData(criteria);
        if (selectedRecord != null) {
            grid.selectRecord(selectedRecord);
        }
    }
}
