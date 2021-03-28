package org.mpm.client.header;

import com.smartgwt.client.widgets.toolbar.ToolStripButton;

public class ImportButton extends ToolStripButton {

    @Override
    public String getInnerHTML() {
        // <style> #image_uploads {  opacity:0; }</style>
        return "<div style='margin: 0;\n"
                + "  position: absolute;\n"
                + "  top: 50%;\n"
                + "  -ms-transform: translateY(-50%);\n"
                + "  transform: translateY(-50%);'>"
                + "<input style='opacity:0;' type='file' id='image_uploads' webkitdirectory onchange='uploadFiles(this.files)'>"
                + "<label for='image_uploads'>导入图片</label></div>";
        // return super.getInnerHTML();
    }
}
