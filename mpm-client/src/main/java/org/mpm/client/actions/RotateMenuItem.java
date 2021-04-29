package org.mpm.client.actions;

import com.smartgwt.client.widgets.menu.MenuItem;
import java.util.HashMap;
import org.mpm.client.PicsGrid;

public class RotateMenuItem extends MenuItem {

    public RotateMenuItem(String title, int degree) {
        super(title);
        addClickHandler(menuItemClickEvent -> {
            HashMap values = new HashMap();
            values.put("rotate", degree);
            PicsGrid.instance.updateSelectedPhotos(values);
        });
    }
}
