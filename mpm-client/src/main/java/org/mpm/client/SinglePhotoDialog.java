package org.mpm.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import org.mpm.client.events.PicsChangeEvent;


public class SinglePhotoDialog extends Canvas {

    HTMLPane pane = new HTMLPane();
    PicsGrid picsGrid;
    Record record;

    public SinglePhotoDialog(PicsGrid picsGrid) {
        setWidth100();
        setHeight100();
        this.picsGrid = picsGrid;

        pane.setWidth100();
        pane.setHeight100();
        this.setAlwaysShowScrollbars(false);
        pane.setAlwaysShowScrollbars(false);

        addChild(pane);
        hide();
        Page.registerKey("D", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                if (record != null) {
                    int index = picsGrid.getRecordIndex(record);
                    SC.logWarn("Delete current..." + index);
                    Record nextRecord = index == picsGrid.getResultSet().getLength() - 1
                            ? picsGrid.getTileRecord(picsGrid.getTile(index - 1))
                            : picsGrid.getTileRecord(picsGrid.getTile(index + 1));
                    picsGrid.selectRecord(record);
                    picsGrid.removeSelectedData();
                    PhotoManagerEntryPoint.eventBus.fireEvent(
                            new PicsChangeEvent(picsGrid.getResultSet().getLength() - 1));
                    setPhoto(nextRecord);
                }
            }
        });
        Page.registerKey(KeyNames.ESC, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                record = null;
                hide();
            }
        });
    }

    public void setPhoto(Record record) {
        this.record = record;
        picsGrid.deselectAllRecords();

        SC.logWarn("Name :" + record.getAttribute("name"));
        pane.setContents("<img src=\""
                + ServerConfig.thumbUrl + record.getAttribute("name")
                + "\" style=\"object-fit:contain;display:block;padding:5px\""
                + " width=" + (pane.getWidth() - 10)
                + " height=" + (pane.getHeight() - 10)
                + "/>"
        );

        pane.redraw("xx");
        // pane.();
        show();
    }
}
