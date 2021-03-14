package org.mpm.client.header;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.PhotoManagerEntryPoint;
import org.mpm.client.PicsGrid;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.events.PicsChangeHandler;

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

    }
}
