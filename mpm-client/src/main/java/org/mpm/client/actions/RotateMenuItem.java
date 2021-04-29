package org.mpm.client.actions;

import com.smartgwt.client.widgets.menu.MenuItem;
import java.util.HashMap;
import org.mpm.client.PicsGrid;
import org.mpm.client.SinglePhotoDialog;

public class RotateMenuItem extends MenuItem {

    public RotateMenuItem(String title, int degree) {
        super(title);
        addClickHandler(menuItemClickEvent -> {
            SinglePhotoDialog photoDialog = PicsGrid.instance.getSinglePhotoDialog();
            if (photoDialog.isShow()) {
                photoDialog.rotateCurrentRecord(degree);
            } else {
                HashMap values = new HashMap();
                values.put("rotate", degree);
                PicsGrid.instance.updateSelectedPhotos(values);
            }
        });
    }
}
