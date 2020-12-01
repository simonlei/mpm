package org.mpm.client;

import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.Offline;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.events.ButtonClickEvent;
import com.smartgwt.client.widgets.events.ButtonClickHandler;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

public class HeaderPanel extends HLayout {

    public HeaderPanel() {
        super();
        ToolStrip toolStrip = new ToolStrip();
        ToolStripButton uploadButton = new ToolStripButton("导入照片");
        uploadButton.addClickHandler(clickEvent -> {
            Dialog dialog = new Dialog();
            dialog.setTitle("导入照片");
            // dialog.setMessage("请选择目录");
            dialog.setWidth(800);
            dialog.setHeight(600);
            dialog.setIsModal(true);
/*
            FormPanel form = new FormPanel();
            FileUpload upload = new FileUpload();
            upload.setName("files");
            upload.getElement().setPropertyBoolean("webkitdirectory", true);
            form.setAction("/uploadImages");
            form.setEncoding(FormPanel.ENCODING_MULTIPART);
            form.setMethod(FormPanel.METHOD_POST);
            form.add(upload);

            dialog.addItem(form);
*/
            TreeGrid grid = new TreeGrid();
            RestDataSource dataSource = new RestDataSource();
            dataSource.setID("fileSystem");
            dataSource.setDataFormat(DSDataFormat.JSON);
            dataSource.setFetchDataURL("/fileSystem/fetch");
            dataSource.setJsonPrefix("");
            dataSource.setJsonSuffix("");
            DataSourceField pathField = new DataSourceField("path", FieldType.TEXT);
            pathField.setPrimaryKey(true);
            dataSource.addField(pathField);
            dataSource.addField(new DataSourceField("name", FieldType.TEXT));
            DataSourceField parentField = new DataSourceField("parent", FieldType.TEXT);
            parentField.setForeignKey("path");

            dataSource.addField(parentField);

//            dataSource.setCacheAllData(true);
            grid.setDataSource(dataSource);

            grid.setAutoFetchData(true);
            grid.setLoadDataOnDemand(true);
            grid.setWidth100();
            grid.setHeight100();

            String selected = (String) Offline.get("Selected.Folder");
            SC.logWarn("Selected " + selected);
/*
            if (selected != null && selected.length() > 0) {
                // init selected nodes
                String[] paths = selected.split("/");
                TreeNode[] nodes = new TreeNode[paths.length];
                String fullPath = "";
                Tree tree = new Tree();
                for (int i = 0; i < paths.length; i++) {
                    String path = paths[i];
                    TreeNode treeNode = new TreeNode();
                    treeNode.setParentID(fullPath);
                    tree.add(treeNode, fullPath);
                    fullPath += "/" + path;
                    treeNode.setID(fullPath);
                    treeNode.setName(path);
                    SC.logWarn("I " + i + " path " + path + " fullPath " + fullPath);
                    nodes[i] = treeNode;
                }
                grid.setData(tree);
//                grid.setInitialData(nodes);
            }
*/
            // grid.setData();

            dialog.addItem(grid);

            dialog.setButtons(new Button("OK"));
            dialog.addButtonClickHandler(new ButtonClickHandler() {
                public void onButtonClick(ButtonClickEvent event) {
                    dialog.hide();
                    TreeNode selected = grid.getSelectedRecord();

                    SC.say("Tree select " + selected.getAttribute("path"));
                    grid.getSelectedPaths();
                    Offline.put("Selected.Folder", selected.getAttribute("path"));
                    // SC.say("Tree select " + grid.getSelectedRecord());
//                    form.submit();
//                    SC.say("Event : " + upload.getFilename());
                }
            });

            dialog.draw();

            grid.addDataArrivedHandler((DataArrivedHandler) dataArrivedEvent -> {

                SC.logWarn("dataarrive " + dataArrivedEvent.getStartRow());
                SC.logWarn("dataarrive " + dataArrivedEvent.getEndRow());
                SC.logWarn("dataarrive " + dataArrivedEvent.toDebugString());

                SC.logWarn("records arrive " + grid.getRecords().length);
                String[] attributes = grid.getRecords()[0].getAttributes();
                for (String att : attributes) {
                    SC.logWarn(att + ":" + grid.getRecords()[0].getAttribute(att));
                }

                TreeNode[] allNodes = grid.getTree().getAllNodes();
                //TreeNode lastNode = null;

                for (int i = allNodes.length - 1; i >= 0; i--) {
                    String path = allNodes[i].getAttribute("path");
                    SC.logWarn(path);
                    if (selected != null && selected.contains(path)) {
                        grid.deselectAllRecords();
                        grid.selectRecord(i);
                        grid.scrollToRow(i);
                        SC.logWarn("Select " + i);
                        grid.openFolder(allNodes[i]);
                        break;
                    }
                }

                // grid.getTree().getAllNodes()[0];
                /*
                 TreeNode node = grid.getTree().getAllNodes()[0];
                 grid.selectRecord(0);

                 grid.openFolder(node);
                 */
                // grid.fetchRelatedData(grid.getRecords()[0], dataSource);
                // grid.expandRecord(grid.getRecords()[0]);
                // grid.refreshData();
                // grid.redraw();
            });

            //Tree select [ "///Users//Users/simon" ]

            // grid.expandRecords();
            grid.setSelectionType(SelectionStyle.SINGLE);
            grid.setShowRoot(true);
            SC.logWarn("records " + grid.getRecords().length);
            // grid.expandRecord(grid.getRecords()[0]);
            // grid.setSelectedPaths("[ \"///Users//Users/simon\" ]");
            // grid.refreshData();
        });
        toolStrip.addButton(uploadButton);

/*        FileUpload upload = new FileUpload();
        upload.getElement().setPropertyBoolean("webkitdirectory", true);
        // FormItem item = new FormItem(upload);
        upload.setTitle("导入照片");
        toolStrip.addMember(upload);
*/
        addMember(toolStrip);
    }
}
