package org.mpm.client.events;

import com.google.gwt.core.client.JavaScriptObject;
import com.smartgwt.client.event.AbstractSmartEvent;

public class PicsCountChangeEvent extends AbstractSmartEvent<PicsCountChangeHandler> {

    public static Type<PicsCountChangeHandler> type = new Type<PicsCountChangeHandler>();
    int count;

    public PicsCountChangeEvent(int count) {
        super(JavaScriptObject.createObject());
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Type<PicsCountChangeHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(PicsCountChangeHandler handler) {
        handler.onDataChanged(this);
    }

}
