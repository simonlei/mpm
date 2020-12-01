package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SplitPane;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

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
        VLayout root = new VLayout();
        root.setWidth100();
        root.setHeight100();
        root.setPadding(20);

        root.addMember( new HeaderPanel());
        root.addMember( createContentPane());
  //      layout.addMember(grid);

        root.draw();
    }

    private SplitPane createContentPane() {
        SplitPane contentPane = new SplitPane();
        contentPane.setWidth100();
        contentPane.setHeight100();

        contentPane.setNavigationPane( createLeftPanel());
        contentPane.setListPane( createPicsGrid());
        return contentPane;
    }

    private TileGrid createPicsGrid() {
	    return new TileGrid();
    }

    private VLayout createLeftPanel() {
	    return new VLayout();
    }
}