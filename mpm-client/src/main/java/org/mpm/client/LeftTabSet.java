package org.mpm.client;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.rpc.DMI;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;
import java.util.HashMap;

public class LeftTabSet extends TabSet {

    public static LeftTabSet instance;
    TreeNode lastSelectedNode = null;
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
                lastSelectedNode = null;
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

    public static void staticReload() {
        instance.reloadData();
    }

    public static native void exportReloadData() /*-{
      $wnd.realodLeftTab = $entry(@org.mpm.client.LeftTabSet::staticReload());
    }-*/;

    private void addFilesTab() {
        Tab filesTab = new Tab("按图片库查看");
        filesGrid = new TreeGrid();
        filesGrid.setShowHeader(false);
        filesGrid.setShowRoot(false);
        DataSource dataSource = DataSource.get("filesTree");
        filesGrid.setFields(new TreeGridField("title"));
        filesGrid.setDataSource(dataSource);
        filesGrid.setAutoFetchData(false);
        Menu menu = new Menu();
        addBatchSetDateMenu(menu);
        addBatchSetGisMenu(menu);
        addBatchSetDescMenu(menu);
        addTrashAllMenu(menu);
        filesGrid.setContextMenu(menu);
        filesGrid.addNodeContextClickHandler(
                nodeContextClickEvent -> filesGridClick(nodeContextClickEvent.getNode()));
        filesGrid.addNodeClickHandler(nodeClickEvent -> filesGridClick(nodeClickEvent.getNode()));
        filesGrid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {
            selectLastRecord(filesGrid);
        });
        filesTab.setPane(filesGrid);
        addTab(filesTab);
    }

    private void addTrashAllMenu(Menu menu) {
        MenuItem trashItem = new MenuItem("删除目录下照片");
        trashItem.setDynamicTitleFunction(
                (canvas, menu1, menuItem) -> PicsGrid.isTrashed() ? "恢复目录下照片" : "删除目录下照片");
        trashItem.addClickHandler(hanler -> {
            SC.confirm(PicsGrid.isTrashed() ? "确定恢复照片？" : "确定删除照片？", aBoolean -> {
                if (Boolean.TRUE == aBoolean) {
                    HashMap values = new HashMap();
                    values.put("trashed", !PicsGrid.isTrashed());
                    batchUpdateValues(values);
                }
            });
        });
        menu.addItem(trashItem);
    }

    private void addBatchSetGisMenu(Menu menu) {
        MenuItem gisItem = new MenuItem("批量修改位置信息");
        gisItem.addClickHandler(menuItemClickEvent -> {
            SC.askforValue("请输入Gis信息， 经度,纬度", s -> {
                if (s != null && s.trim().length() > 0) {
                    HashMap values = new HashMap();
                    String[] strs = s.split(",");
                    values.put("longitude", strs[0]);
                    values.put("latitude", strs[1]);
                    batchUpdateValues(values);
                }
            });
        });
        menu.addItem(gisItem);
    }

    private void batchUpdateValues(HashMap values) {
        DMI.call("mpm", "org.mpm.server.pics.PicsDataSource", "batchUpdatePics",
                (rpcResponse, o, rpcRequest) -> {
                    reloadData();
                    PicsGrid.instance.reloadData();
                }, new Object[]{PicsGrid.isTrashed(), criteria.getValues(), values});
    }

    private void filesGridClick(TreeNode node) {
        lastSelectedNode = node;
        criteria = new Criteria();
        criteria.addCriteria("filePath", node.getAttributeAsInt("path"));
        PicsGrid.instance.reloadData();
        SC.logWarn("reload fileid");
        filesGrid.openFolder(node);
    }

    private void addDatesTab() {
        Tab datesTab = new Tab("按时间查看");
        datesGrid = new TreeGrid();
        //datesGrid.menu
        Menu menu = new Menu();
        addBatchSetDateMenu(menu);
        addBatchSetGisMenu(menu);
        addBatchSetDescMenu(menu);
        addTrashAllMenu(menu);
        datesGrid.setContextMenu(menu);
        datesGrid.setShowHeader(false);
        DataSource dataSource = DataSource.get("datesTree");

        datesGrid.setFields(new TreeGridField("title"));
        datesGrid.setDataSource(dataSource);
        datesGrid.setAutoFetchData(false);
        datesGrid.setLoadDataOnDemand(false);
        datesGrid.addNodeContextClickHandler(
                nodeContextClickEvent -> datesGridClick(nodeContextClickEvent.getNode()));
        datesGrid.addNodeClickHandler(nodeClickEvent -> datesGridClick(nodeClickEvent.getNode()));
        datesGrid.addDataArrivedHandler(
                (DataArrivedHandler) dataArrivedEvent -> selectLastRecord(datesGrid));
        datesTab.setPane(datesGrid);
        addTab(datesTab);
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        datesGrid.fetchData(criteria);
    }

    private void addBatchSetDescMenu(Menu menu) {
        MenuItem dateItem = new MenuItem("批量设置描述信息");
        dateItem.addClickHandler(menuItemClickEvent -> {
            SC.askforValue("请输入描述信息", s -> {
                if (s != null && s.trim().length() > 0) {
                    HashMap values = new HashMap();
                    values.put("description", s);
                    batchUpdateValues(values);
                }
            });
        });
        menu.addItem(dateItem);
    }

    private void datesGridClick(TreeNode node) {
        lastSelectedNode = node;
        criteria = new Criteria();
        if (node.getAttribute("year") != null) {
            criteria.addCriteria("theYear", node.getAttribute("year"));
        }
        if (node.getAttribute("month") != null) {
            criteria.addCriteria("theMonth", node.getAttribute("month"));
        }
        PicsGrid.instance.reloadData();
        SC.logWarn("Click:" + node.getTitle() + " Criteria " + criteria.getValues());
        datesGrid.openFolder(node);
    }

    private void addBatchSetDateMenu(Menu menu) {
        MenuItem dateItem = new MenuItem("批量设置时间");
        dateItem.addClickHandler(menuItemClickEvent -> {
            SC.askforValue("请输入日期 yyyy-MM-dd", s -> {
                if (s != null && s.trim().length() > 0) {
                    HashMap values = new HashMap();
                    values.put("takenDate", s);
                    batchUpdateValues(values);
                }
            });
        });
        menu.addItem(dateItem);
    }

    private void selectLastRecord(TreeGrid grid) {
        if (lastSelectedNode != null) {
            TreeNode[] allNodes = grid.getTree().getAllNodes();

            for (int i = 0; i < allNodes.length; i++) {
                String id = allNodes[i].getAttribute("id");
                if (id.equals(lastSelectedNode.getAttribute("id"))) {
                    grid.selectRecord(allNodes[i]);
                    grid.openFolder(allNodes[i]);
                    return;
                }
            }
        } else {
            grid.selectRecord(0);
            grid.openFolder(grid.getRecord(0));
        }
    }

    /**
     * pics grid删除/导入图片时，左侧树要重新加载
     */
    public void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", PicsGrid.isTrashed());
        SC.logWarn("Selected tab: " + selectedTab);
        TreeGrid grid = selectedTab == 0 ? datesGrid : filesGrid;

        grid.setCriteria(criteria);
        if (grid == filesGrid && !filesTabInited) {
            filesTabInited = true;
            grid.fetchData(criteria);
        } else {
            grid.invalidateCache();
        }
    }
}
