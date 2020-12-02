package org.mpm.client;

import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.Offline;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import java.util.HashMap;
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
                dialog.hide();
                TreeNode selected1 = grid.getSelectedRecord();

                String path = selected1.getAttribute("path");
                Offline.put("Selected.Folder", path);
                RPCRequest req = new RPCRequest();
                req.setUseSimpleHttp(true);

                req.setActionURL("/fileSystem/importImages");
                // req.setContentType("text/json");
                HashMap data = new HashMap();
                data.put("folder", path);
                req.setParams(data);

                RPCManager.sendRequest(req,
                        (rpcResponse, o, rpcRequest) -> SC.logWarn("response " + rpcResponse));
            });

            dialog.draw();
        });
        toolStrip.addButton(uploadButton);

        addMember(toolStrip);
    }
}
