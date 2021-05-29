package org.mpm.client;

import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import org.mpm.client.PicsGrid.GridKeyHandler;
import org.mpm.client.SinglePhotoDialog.SinglePageKeyHandler;

public class KeyHandlers {

    static void initDebugKey() {
        KeyIdentifier debugKey = new KeyIdentifier();
        debugKey.setCtrlKey(true);
        debugKey.setKeyName("E");

        Page.registerKey(debugKey, new PageKeyHandler() {
            public void execute(String keyName) {
                SC.showConsole();
            }
        });
    }

    public static void initKeys(PicsGrid picsGrid) {
        SinglePhotoDialog singlePhotoDialog = picsGrid.getSinglePhotoDialog();

        GridKeyHandler gridKeyHandler = picsGrid.new GridKeyHandler();
        Page.registerKey(KeyNames.ENTER, gridKeyHandler);
        Page.registerKey("D", gridKeyHandler);
        Page.registerKey("R", gridKeyHandler);

        SinglePageKeyHandler pageKeyHandler = singlePhotoDialog.new SinglePageKeyHandler();
        Page.registerKey(KeyNames.ESC, pageKeyHandler);
        Page.registerKey(KeyNames.ARROW_LEFT, pageKeyHandler);
        Page.registerKey(KeyNames.ARROW_RIGHT, pageKeyHandler);
        Page.registerKey(KeyNames.SPACE, pageKeyHandler);
        Page.registerKey("I", pageKeyHandler);
        Page.registerKey("S", pageKeyHandler);
        Page.registerKey("J", pageKeyHandler);
        Page.registerKey("K", pageKeyHandler);
        Page.registerKey("L", pageKeyHandler);
        Page.registerKey("H", pageKeyHandler);
        Page.registerKey("D", pageKeyHandler);
        Page.registerKey("R", pageKeyHandler);
    }
}
