package org.mpm.client;

import com.google.gwt.user.client.Window;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.events.PicsChangeHandler;
import org.mpm.client.util.ClientUtils;

public class SwitchTrashButton extends ToolStripButton implements PicsChangeHandler, ClickHandler {

    public SwitchTrashButton() {
        super("回收站(0)");
        PhotoManagerEntryPoint.eventBus.addHandler(PicsChangeEvent.type, this);
        addClickHandler(this);
    }

    @Override
    public void onDataChanged(PicsChangeEvent evt) {
        Window.setTitle("My photos manager - " + evt.getCount() + " 张");
        // set text for self.
        RPCRequest countRequest = ClientUtils
                .makeRPCRequest("/pics/count", "trashed", !PicsGrid.isTrashed());
        RPCManager.sendRequest(countRequest, new RPCCallback() {
            @Override
            public void execute(RPCResponse rpcResponse, Object o, RPCRequest rpcRequest) {
                setTitle(PicsGrid.isTrashed()
                        ? "正常照片(" + rpcResponse.getDataAsString() + ")"
                        : "回收站(" + rpcResponse.getDataAsString() + ")");
            }
        });
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        PicsGrid.setTrashed(!PicsGrid.isTrashed());
    }
}
