package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.rpc.DMI;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.SplitPane;
import com.smartgwt.client.widgets.layout.VLayout;
import java.util.Map;
import org.mpm.client.header.HeaderPanel;

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
        DMI.call("mpm", "org.mpm.server.metas.ConfigDataSource", "fetchConfig",
                (rpcResponse, o, rpcRequest) -> {
                    Map map = rpcResponse.getDataAsMap();
                    SC.logWarn("thumbUrl is :" + map.get("thumbUrl"));
                    ServerConfig.thumbUrl = (String) map.get("thumbUrl");
                }, null);
    }

    private SplitPane createContentPane() {
        SplitPane contentPane = new SplitPane();
        contentPane.setWidth100();
        contentPane.setHeight100();
        contentPane.setShowListToolStrip(false);

        contentPane.setNavigationPane(new LeftTabSet());
        contentPane.setListPane(new PicsGrid());
        return contentPane;
    }

}