package org.mpm.client.header;

import com.smartgwt.client.widgets.toolbar.ToolStripButton;

public class ImportButton extends ToolStripButton {

    @Override
    public String getInnerHTML() {
        return "<input type='file' webkitdirectory onchange='uploadFiles(this.files)'/>";
        // return super.getInnerHTML();
    }
}
