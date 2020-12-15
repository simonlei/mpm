package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.SplitPane;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tile.TileGrid;
import java.util.Map;
import org.mpm.client.util.ClientUtils;

public final class PhotoManagerEntryPoint implements EntryPoint {

    public static EventBus eventBus = new SimpleEventBus();

    public void onModuleLoad() {

        KeyIdentifier debugKey = new KeyIdentifier();
        debugKey.setCtrlKey(true);
        debugKey.setKeyName("E");

        Page.registerKey(debugKey, new PageKeyHandler() {
            public void execute(String keyName) {
                SC.showConsole();
            }
        });

        loadServerConfig();

        VLayout root = new VLayout();
        root.setWidth100();
        root.setHeight100();
        root.setPadding(20);

        root.addMember(new HeaderPanel());
        root.addMember(createContentPane());

        root.draw();
    }

    private void loadServerConfig() {
        RPCRequest req = ClientUtils.makeRPCRequest("/meta/config");
        RPCManager.sendRequest(req, (rpcResponse, o, rpcRequest) -> {
            Map map = ClientUtils.getResponseAsMap(rpcResponse);
            ServerConfig.thumbUrl = (String) map.get("thumbUrl");
        });
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