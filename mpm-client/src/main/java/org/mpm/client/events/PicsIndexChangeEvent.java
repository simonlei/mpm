package org.mpm.client.events;

import com.google.gwt.core.client.JavaScriptObject;
import com.smartgwt.client.event.AbstractSmartEvent;

public class PicsIndexChangeEvent extends AbstractSmartEvent<PicsIndexChangeHandler> {

    public static Type<PicsIndexChangeHandler> type = new Type<>();
    int index;

    public PicsIndexChangeEvent(int index) {
        super(JavaScriptObject.createObject());
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Type<PicsIndexChangeHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(PicsIndexChangeHandler handler) {
        handler.onDataChanged(this);
    }

}
