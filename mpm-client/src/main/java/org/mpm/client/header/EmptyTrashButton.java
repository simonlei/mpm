package org.mpm.client.header;

import com.smartgwt.client.rpc.DMI;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.LeftTabSet;
import org.mpm.client.PhotoManagerEntryPoint;
import org.mpm.client.PicsGrid;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.events.PicsChangeHandler;
import org.mpm.client.progress.ProgressDialog;

class EmptyTrashButton extends ToolStripButton implements PicsChangeHandler, ClickHandler {

    public EmptyTrashButton() {
        super("清空回收站");
        PhotoManagerEntryPoint.eventBus.addHandler(PicsChangeEvent.type, this);
        addClickHandler(this);
    }

    @Override
    public void onDataChanged(PicsChangeEvent var1) {
        setVisible(PicsGrid.isTrashed());
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        SC.confirm("确定清空回收站？", new BooleanCallback() {
            @Override
            public void execute(Boolean confirm) {
                if (confirm) {
                    DMI.call("mpm", "org.mpm.server.pics.PicsDataSource", "emptyTrash",
                            (rpcResponse, o, rpcRequest) -> {
                                String taskId = rpcResponse.getDataAsString();
                                SC.logWarn("taskid: " + taskId);
                                new ProgressDialog("正在清空回收站...", taskId, () -> {
                                    PicsGrid.instance.reloadData();
                                    LeftTabSet.instance.reloadData();
                                },
                                        "已删除 $count$/$total$ 照片",
                                        "清空完成，共删除 $total$ 张照片").show();

                            }, new Object[]{});
                }
            }
        });
    }
}
