package org.mpm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.tile.SimpleTile;
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

    static Label getHoverComponent(Record record) {
        return new Label(getHoverString(record, true));
    }

    static String getHoverString(Record record, boolean useBr) {
        String address = record.getAttribute("address");
        DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
        Date takenDate = record.getAttributeAsDate("takenDate");
        Long size = record.getAttributeAsLong("size");
        String breakStr = useBr ? "<br/>" : "\n";

        String hover = "大小：" + sizeFormat(size) + breakStr
                + "宽度：" + record.getAttribute("width") + "px" + breakStr
                + "高度：" + record.getAttribute("height") + "px" + breakStr
                // + "描述：" + record.getAttribute("description") + "<br/>"
                + (address == null ? "" : "地址：" + address + breakStr)
                + "时间：" + format.format(takenDate);
        return hover;
    }

    private static String sizeFormat(Long size) {
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

    @Override
    public Canvas getHoverComponent() {
        return getHoverComponent(getRecord());
    }
}
