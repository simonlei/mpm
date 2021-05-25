package org.mpm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import org.mpm.client.PicsGrid.ImageCellMetaFactory;
import org.mpm.client.header.HeaderPanel;

public final class PhotoManagerEntryPoint implements EntryPoint {

    public static EventBus eventBus = new SimpleEventBus();

    public void onModuleLoad() {
        PicsGrid.exportReloadData();
        GWT.create(ImageCellMetaFactory.class);

        KeyIdentifier debugKey = new KeyIdentifier();
        debugKey.setCtrlKey(true);
        debugKey.setKeyName("E");

        Page.registerKey(debugKey, new PageKeyHandler() {
            public void execute(String keyName) {
                SC.showConsole();
            }
        });

        loadServerConfig();

/*        Notify.configureDefaultSettings(new NotifySettings().setDuration(100)
                        //.setMultiMessageMode(MultiMessageMode.STACK)
                        .setMaxStackSize(10)
                //.setMaxStackDismissMode(MaxStackDismissMode.OLDEST)
                //.setAppearMethod(NotifyTransition.INSTANT));
        );
*/
        SC.askforValue("What's the password?",
                s -> DMI.call("mpm", "org.mpm.server.metas.ConfigDataSource", "authPassword",
                        (rpcResponse, o, rpcRequest) -> {
                            Map map = rpcResponse.getDataAsMap();
                            SC.logWarn("response is :" + map.get("ok"));
                            if ("ok".equals(map.get("ok"))) {
                                createMainContent();
                            } else {
                                SC.showPrompt("密码错误，刷新页面重试");
                            }
                        }, new Object[]{s}));
    }

    private void createMainContent() {
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

        contentPane.setListPane(new PicsGrid());
        contentPane.setNavigationPane(new LeftTabSet());
        return contentPane;
    }

}