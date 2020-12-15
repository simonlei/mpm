package org.mpm.client.events;

import com.google.gwt.core.client.JavaScriptObject;
import com.smartgwt.client.event.AbstractSmartEvent;

public class PicsChangeEvent extends AbstractSmartEvent<PicsChangeHandler> {

    public static Type<PicsChangeHandler> type = new Type<PicsChangeHandler>();
    int count;

    public PicsChangeEvent(int count) {
        super(JavaScriptObject.createObject());
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Type<PicsChangeHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(PicsChangeHandler handler) {
        handler.onDataChanged(this);
    }

}
