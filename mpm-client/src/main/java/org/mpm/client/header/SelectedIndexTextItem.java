package org.mpm.client.header;

import com.smartgwt.client.widgets.form.fields.TextItem;
import org.mpm.client.PhotoManagerEntryPoint;
import org.mpm.client.events.PicsIndexChangeEvent;
import org.mpm.client.events.PicsIndexChangeHandler;

public class SelectedIndexTextItem extends TextItem implements PicsIndexChangeHandler {

    public SelectedIndexTextItem() {
        setWidth(30);
        setAttribute("editorType", "TextItem");
        setShowTitle(false);
        setKeyPressFilter("[0-9.]");

        PhotoManagerEntryPoint.eventBus.addHandler(PicsIndexChangeEvent.type, this);
    }

    @Override
    public void onDataChanged(PicsIndexChangeEvent evt) {
        setValue(evt.getIndex() + 1);
    }
}
