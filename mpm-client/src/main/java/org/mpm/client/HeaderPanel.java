package org.mpm.client;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.JSOHelper;
import com.smartgwt.client.util.JSON;
import com.smartgwt.client.util.Offline;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Progressbar;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import java.util.Map;
import org.mpm.client.util.Utils;

public class HeaderPanel extends HLayout {

    public HeaderPanel() {
        super();
        ToolStrip toolStrip = new ToolStrip();
        ToolStripButton uploadButton = new ToolStripButton("导入照片");
        uploadButton.addClickHandler(clickEvent -> {
            Dialog dialog = new Dialog();
            dialog.setTitle("导入照片");

            dialog.setWidth(800);
            dialog.setHeight(600);
            dialog.setIsModal(true);

            TreeGrid grid = new TreeGrid();
            RestDataSource dataSource = Utils.createDataSource("fileSystem", "/fileSystem/fetch");
            DataSourceField pathField = new DataSourceField("path", FieldType.TEXT);
            pathField.setPrimaryKey(true);
            dataSource.addField(pathField);
            dataSource.addField(new DataSourceField("name", FieldType.TEXT));
            DataSourceField parentField = new DataSourceField("parent", FieldType.TEXT);
            parentField.setForeignKey("path");
            dataSource.addField(parentField);

            grid.setDataSource(dataSource);

            grid.setAutoFetchData(true);
            grid.setLoadDataOnDemand(true);
            grid.setWidth100();
            grid.setHeight100();
            grid.setSelectionType(SelectionStyle.SINGLE);
            grid.setShowRoot(true);
            String selected = (String) Offline.get("Selected.Folder");
            SC.logWarn("Selected " + selected);
            grid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {
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
            });

            dialog.addItem(grid);
            dialog.setButtons(new Button("OK"));
            dialog.addButtonClickHandler(event -> {
                dialog.close();
                TreeNode selected1 = grid.getSelectedRecord();

                String path = selected1.getAttribute("path");
                Offline.put("Selected.Folder", path);
                RPCRequest req = Utils.makeRPCRequest("/fileSystem/importImages", "folder", path);

                RPCManager.sendRequest(req, (rpcResponse, o, rpcRequest) -> {
                    String taskId = rpcResponse.getDataAsString();
                    SC.logWarn("taskid: " + taskId);
                    showProgressDialog(taskId);
                });
            });

            dialog.draw();
        });
        toolStrip.addButton(uploadButton);

        addMember(toolStrip);
    }

    private void showProgressDialog(String taskId) {
        Dialog dialog = new Dialog();
        dialog.setTitle("正在导入照片...");
        dialog.setIsModal(true);
        dialog.setShowCloseButton(false);

        Label progressLabel = new Label();
        progressLabel.setContents("正在导入");
        dialog.addItem(progressLabel);

        Progressbar progressbar = new Progressbar();
        progressbar.setPercentDone(0);
        dialog.addItem(progressbar);
        dialog.setHeight(300);
        dialog.draw();

        new Timer() {
            @Override
            public void run() {
                RPCRequest req = Utils
                        .makeRPCRequest("/fileSystem/importProgress", "taskId", taskId);
                RPCManager.sendRequest(req, (rpcResponse, o, rpcRequest) -> {

                    Map result = JSOHelper.convertToMap(JSON.decode(rpcResponse.getDataAsString()));
                    // rpcResponse.getDataAsMap();
                    Object count = result.get("count");
                    Object total = result.get("total");
                    Object picsCount = result.get("picsCount");
                    Integer progress = (Integer) result.get("progress");
                    SC.logWarn("Progress " + rpcResponse.getDataAsString());
                    SC.logWarn("count " + count + " total " + total + " progress " + progress);
                    progressLabel
                            .setContents("已扫描 " + count + "/" + total + " 文件，其中照片数 " + picsCount);
                    progressbar.setPercentDone(progress);

                    if (progress < 100) {
                        schedule(500);
                    } else {
                        dialog.close();
                        SC.say("导入完成，共导入 " + picsCount + " 张图片");
                    }

                });
            }
        }.schedule(500);
    }
}
