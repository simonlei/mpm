package org.mpm.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;


public class SinglePhotoDialog extends Canvas {

    HTMLPane pane = new HTMLPane();

    public SinglePhotoDialog() {
        setWidth100();
        setHeight100();

        pane.setWidth100();
        pane.setHeight100();
        this.setAlwaysShowScrollbars(false);
        pane.setAlwaysShowScrollbars(false);

        addChild(pane);

        Page.registerKey(KeyNames.ESC, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                hide();
            }
        });
    }

    public void setPhoto(Record record) {
        pane.setContents("<img src=\""
                + ServerConfig.thumbUrl + record.getAttribute("name")
                + "\" style=\"object-fit:contain;display:block;padding:5px\""
                + " width=" + (pane.getWidth() - 10)
                + " height=" + (pane.getHeight() - 10)
                + "/>"
        );
        show();
    }
}
