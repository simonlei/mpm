package org.mpm.client.header;

import com.google.gwt.user.client.Window;
import com.smartgwt.client.rpc.DMI;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.PhotoManagerEntryPoint;
import org.mpm.client.PicsGrid;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.events.PicsChangeHandler;

public class SwitchTrashButton extends ToolStripButton implements PicsChangeHandler, ClickHandler {

    private boolean trashed;

    public SwitchTrashButton() {
        super("回收站(0)");
        PhotoManagerEntryPoint.eventBus.addHandler(PicsChangeEvent.type, this);
        addClickHandler(this);
    }

    @Override
    public void onDataChanged(PicsChangeEvent evt) {
        Window.setTitle("My photos manager - " + evt.getCount() + " 张");
        // set text for self.
        DMI.call("mpm", "org.mpm.server.pics.PicsDataSource", "count",
                (rpcResponse, o, rpcRequest) ->
                        setTitle(PicsGrid.isTrashed()
                                ? "正常照片(" + rpcResponse.getDataAsString() + ")"
                                : "回收站(" + rpcResponse.getDataAsString() + ")")
                , new Object[]{!PicsGrid.isTrashed()});
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        trashed = !PicsGrid.isTrashed();
        PicsGrid.setTrashed(trashed);
    }

    public boolean isTrashed() {
        return trashed;
    }
}
