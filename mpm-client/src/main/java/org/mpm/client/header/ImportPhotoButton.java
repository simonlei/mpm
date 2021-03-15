package org.mpm.client.header;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.Offline;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import org.mpm.client.LeftTabSet;
import org.mpm.client.PicsGrid;
import org.mpm.client.progress.ProgressDialog;
import org.mpm.client.util.ClientUtils;

public class ImportPhotoButton extends ToolStripButton {

    private String selected = null;
    private boolean autoSelect = true;

    public ImportPhotoButton() {
        super("导入图片");
        addClickHandler(clickEvent -> {
            Dialog dialog = new Dialog();
            dialog.setTitle("导入照片");

            dialog.setWidth(800);
            dialog.setHeight(600);
            dialog.setIsModal(true);

            TreeGrid grid = new TreeGrid();
            DataSource dataSource = DataSource.get("fileSystem");
            grid.setDataSource(dataSource);

            grid.setAutoFetchData(true);
            grid.setLoadDataOnDemand(true);
            grid.setWidth100();
            grid.setHeight100();
            grid.setSelectionType(SelectionStyle.SINGLE);
            grid.setShowRoot(false);
            selected = (String) Offline.get("Selected.Folder");
            SC.logWarn("Selected " + selected);
            grid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {
                autoSelect = true;
                TreeNode[] allNodes = grid.getTree().getAllNodes();

                for (int i = allNodes.length - 1; i >= 0; i--) {
                    String path = allNodes[i].getAttribute("path");
                    if (selected != null && selected.contains(path)) {
                        grid.deselectAllRecords();
                        grid.selectRecord(i);
                        grid.scrollToRow(i);
                        SC.logWarn("Select " + i);
                        grid.openFolder(allNodes[i]);
                        break;
                    }
                }
                autoSelect = false;
            });
            grid.addSelectionChangedHandler(selectionEvent -> {
                if (!autoSelect) {
                    ListGridRecord selectedRecord = selectionEvent.getSelectedRecord();
                    if (selectedRecord != null) {
                        selected = selectedRecord.getAttribute("path");
                    }
                }
            });

            dialog.addItem(grid);
            dialog.setButtons(new Button("OK"));
            dialog.addButtonClickHandler(event -> {
                dialog.close();
                TreeNode selected1 = grid.getSelectedRecord();

                String path = selected1.getAttribute("path");
                Offline.put("Selected.Folder", path);
                RPCRequest req = ClientUtils
                        .makeRPCRequest("/fileSystem/importImages", "folder", path);

                RPCManager.sendRequest(req, (rpcResponse, o, rpcRequest) -> {
                    String taskId = rpcResponse.getDataAsString();
                    SC.logWarn("taskid: " + taskId);
                    new ProgressDialog("正在导入照片...", taskId, () -> {
                        LeftTabSet.instance.reloadData();
                        PicsGrid.reloadData();
                    }, "已扫描 $count$/$total$ 文件，其中照片数 $picsCount$",
                            "导入完成，共导入 $picsCount$ 张图片").show();
                });
            });

            dialog.draw();
        });
    }
}
