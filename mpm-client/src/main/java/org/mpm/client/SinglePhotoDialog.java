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
                    int totalCount = picsGrid.getResultSet().getLength();
                    Record nextRecord;
                    if (totalCount == 1) { // only one, exit
                        nextRecord = null;
                    } else if (index == totalCount - 1) { // last one, get prev one
                        nextRecord = picsGrid.getTileRecord(picsGrid.getTile(index - 1));
                    } else { // get next one
                        nextRecord = picsGrid.getTileRecord(picsGrid.getTile(index + 1));
                    }
                    SC.logWarn("1");
                    picsGrid.deselectAllRecords();
                    SC.logWarn("2");
                    picsGrid.removeData(record);
                    // picsGrid.removeSelectedData();
                    SC.logWarn("3");
                    PhotoManagerEntryPoint.eventBus.fireEvent(new PicsChangeEvent(totalCount - 1));
                    SC.logWarn("4");
                    if (nextRecord == null) {
                        record = null;
                        hide();
                    } else {
                        setPhoto(nextRecord);
                    }
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
        show();
    }
}
