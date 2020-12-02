package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.SplitPane;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tile.TileGrid;

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

        VLayout root = new VLayout();
        root.setWidth100();
        root.setHeight100();
        root.setPadding(20);

        root.addMember(new HeaderPanel());
        root.addMember(createContentPane());

        root.draw();
    }

    private SplitPane createContentPane() {
        SplitPane contentPane = new SplitPane();
        contentPane.setWidth100();
        contentPane.setHeight100();
        contentPane.setShowListToolStrip(false);

        contentPane.setNavigationPane(createLeftPanel());
        contentPane.setListPane(createPicsGrid());
        return contentPane;
    }

    private TileGrid createPicsGrid() {
        return new PicsGrid();
    }

    private VLayout createLeftPanel() {
        return new VLayout();
    }
}