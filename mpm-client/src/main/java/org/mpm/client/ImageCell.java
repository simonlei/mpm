package org.mpm.client;

import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.SimpleTile;

@BeanFactory.Generate
public class ImageCell extends SimpleTile {

    public ImageCell() {
        super();
        // SC.logWarn("ImageCellllllll");
        setOverflow(Overflow.HIDDEN);
        //setHeight(150);
        //setWidth(200);
        setShowSelectedIcon(true);

        addKeyPressHandler(keyPressEvent -> {
            SC.logWarn(keyPressEvent.getKeyName());
        });
    }
}
