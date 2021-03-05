package org.mpm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.tile.SimpleTile;
import com.smartgwt.client.widgets.tile.TileRecord;
import java.util.Date;

@BeanFactory.Generate
public class ImageCell extends SimpleTile {

    public ImageCell() {
        super();
        setOverflow(Overflow.HIDDEN);
        setShowSelectedIcon(true);
        setShowHoverComponents(true);
        setHoverWidth(300);
        setHoverAutoFitWidth(true);
        addKeyPressHandler(keyPressEvent -> {
            SC.logWarn(keyPressEvent.getKeyName());
        });
    }

    @Override
    public Canvas getHoverComponent() {
        TileRecord record = getRecord();

        String address = record.getAttribute("address");
        DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
        Date takenDate = record.getAttributeAsDate("takenDate");
        Long size = record.getAttributeAsLong("size");

        String hover = "大小：" + sizeFormat(size) + "<br/>"
                + "宽度：" + record.getAttribute("width") + "px<br/>"
                + "高度：" + record.getAttribute("height") + "px<br/>"
                // + "描述：" + record.getAttribute("description") + "<br/>"
                + (address == null ? "" : "地址：" + address + "<br/>")
                + "时间：" + format.format(takenDate);
        return new Label(hover);
    }

    private String sizeFormat(Long size) {
        NumberFormat format = NumberFormat.getFormat("0.00");

        if (size >= 1024 * 1024) {
            return format.format((size / (1024.0 * 1024.0))) + "MB";
        } else if (size >= 1024) {
            return format.format((size / (1024.0))) + "KB";
        } else {
            if (size <= 0) {
                return "0B";
            } else {
                return size + "B";
            }
        }
    }
}
