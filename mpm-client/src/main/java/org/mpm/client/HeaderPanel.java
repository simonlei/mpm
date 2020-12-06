package org.mpm.client;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

public class HeaderPanel extends HLayout {

    public HeaderPanel() {
        super();
        ToolStrip toolStrip = new ToolStrip();
        toolStrip.addButton(new ImportPhotoButton());
        toolStrip.addSeparator();
        toolStrip.addButton(new SwitchTrashButton());

        addMember(toolStrip);
    }


}
