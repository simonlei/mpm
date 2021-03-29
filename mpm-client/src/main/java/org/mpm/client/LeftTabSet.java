package org.mpm.client;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class LeftTabSet extends TabSet {

    public static LeftTabSet instance;
    String lastSelectedTitle = null;
    boolean filesTabInited = false;
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
                lastSelectedTitle = null;
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
            PicsGrid.instance.reloadData();
            SC.logWarn("reload fileid");
        });
        filesGrid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {
            selectLastRecord(filesGrid);
        });
        filesTab.setPane(filesGrid);
        addTab(filesTab);
    }

    private void addDatesTab() {
        Tab datesTab = new Tab("按时间查看");
        datesGrid = new TreeGrid();
        datesGrid.setShowHeader(false);
        datesGrid.setShowRoot(true);
        datesGrid.setTreeRootValue("全部");
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
                criteria.addCriteria("theYear", getYearOrMonth(title));
            } else if (title.contains("月")) {
                SC.logWarn("Y:" + getYearOrMonth(node.getAttribute("year")) + " M:"
                        + getYearOrMonth(node.getAttribute("month")));
                criteria.addCriteria("theYear", getYearOrMonth(node.getAttribute("year")));
                criteria.addCriteria("theMonth", getYearOrMonth(node.getAttribute("month")));
            }
            PicsGrid.instance.reloadData();
            SC.logWarn("Click:" + node.getTitle() + " Criteria " + criteria.getValues());
        });
        datesGrid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {
            selectLastRecord(datesGrid);
        });
        datesTab.setPane(datesGrid);
        addTab(datesTab);
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        datesGrid.fetchData(criteria);
    }

    private void selectLastRecord(TreeGrid grid) {
        if (lastSelectedTitle != null) {
            lastSelectedTitle = lastSelectedTitle.replaceAll("\\(.*\\)", "");
            SC.logWarn("lastSelectedRecord is:" + lastSelectedTitle);
            TreeNode[] allNodes = grid.getTree().getAllNodes();

            for (int i = 0; i < allNodes.length; i++) {
                String title = allNodes[i].getAttribute(selectedTab == 0 ? "title" : "name");
                SC.logWarn("Title " + i + " " + title);
                if (title.startsWith(lastSelectedTitle)) {
                    SC.logWarn("Select " + i + " " + title);
                    grid.selectRecord(allNodes[i]);
                    return;
                }
            }
        }

    }

    private boolean notEmpty(String str) {
        return !(str == null || str.trim().length() == 0);
    }

    private String getYearOrMonth(String title) {
        return title.substring(0, title.length() - 1);
    }

    /**
     * pics grid删除/导入图片时，左侧树要重新加载
     */
    public void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", PicsGrid.isTrashed());
        SC.logWarn("Selected tab: " + selectedTab);
        TreeGrid grid = selectedTab == 0 ? datesGrid : filesGrid;
        TreeNode selectedRecord = grid.getSelectedRecord();
        SC.logWarn("Selected Record: " + selectedRecord);
        lastSelectedTitle = selectedRecord == null ? null
                : selectedRecord.getAttribute(selectedTab == 0 ? "title" : "name");

        SC.logWarn("Selected Record Title: " + lastSelectedTitle);
        grid.setCriteria(criteria);
        if (grid == filesGrid && !filesTabInited) {
            filesTabInited = true;
            grid.fetchData(criteria);
        } else {
            grid.invalidateCache();
        }
    }
}
