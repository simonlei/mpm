package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.VLayout;

public final class PhotoManagerEntryPoint implements EntryPoint {

	public void onModuleLoad() {

        KeyIdentifier debugKey = new KeyIdentifier();
        debugKey.setCtrlKey(true);
        debugKey.setKeyName("D");

        Page.registerKey(debugKey, new PageKeyHandler() {
            public void execute(String keyName) {
                SC.showConsole();
            }
        });
/*
        ListGrid grid = new ListGrid();
        grid.setDataSource("Product");
        grid.setAutoFetchData(true);
        grid.setCanEdit(true);
        
        grid.setGroupByField("productLine");
        grid.setSortField("buyPrice");
        grid.setSortByGroupFirst(true);
*/
        VLayout layout = new VLayout();
        layout.setWidth100();
        layout.setHeight100();
        layout.setPadding(20);
            Label label = new Label( "hello world!");
            layout.addMember(label);
  //      layout.addMember(grid);

        layout.draw();
    }

}