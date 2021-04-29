package org.mpm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.tile.SimpleTile;
import com.smartgwt.client.widgets.tile.TileRecord;
import java.util.Date;
import java.util.HashMap;
import org.mpm.client.actions.RotateMenuItem;

@BeanFactory.Generate
public class ImageCell extends SimpleTile {

    private Label durationLabel;

    public ImageCell() {
        super();
        setOverflow(Overflow.HIDDEN);
        setShowSelectedIcon(true);
        setShowHoverComponents(true);
        setHoverWidth(300);
        setHoverAutoFitWidth(true);
        Menu contextMenu = new Menu();
        addModifyDateMenu(contextMenu);
        addModifyGisMenu(contextMenu);
        addRotateMenu(contextMenu);
        setContextMenu(contextMenu);
    }

    static String getHoverString(Record record, boolean useBr) {
        if (record == null) {
            return "";
        }
        String address = record.getAttribute("address");
        DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
        Date takenDate = record.getAttributeAsDate("takenDate");
        Long size = record.getAttributeAsLong("size");
        String breakStr = useBr ? "<br/>" : "\n";

        String hover = "大小：" + sizeFormat(size) + breakStr
                + "宽度：" + record.getAttribute("width") + "px" + breakStr
                + "高度：" + record.getAttribute("height") + "px" + breakStr
                + "描述：" + record.getAttribute("description") + breakStr
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

    private void addRotateMenu(Menu contextMenu) {
        MenuItem rotateMenuItem = new MenuItem("旋转");

        Menu rotateMenu = new Menu();
        rotateMenu.addItem(new RotateMenuItem("左旋90度", -90));
        rotateMenu.addItem(new RotateMenuItem("右旋90度", 90));
        rotateMenu.addItem(new RotateMenuItem("旋转180度", 180));
        rotateMenu.addItem(new RotateMenuItem("重置", 0));
        rotateMenuItem.setSubmenu(rotateMenu);
        contextMenu.addItem(rotateMenuItem);
    }

    private void addModifyGisMenu(Menu contextMenu) {
        MenuItem modifyGis = new MenuItem("修改位置信息");
        modifyGis.addClickHandler(
                menuItemClickEvent -> SC.askforValue("请输入Gis信息， 经度,纬度", s -> {
                    HashMap values = new HashMap();
                    String[] strs = s.split(",");
                    values.put("longitude", strs[0]);
                    values.put("latitude", strs[1]);
                    ((PicsGrid) getTileGrid()).updateSelectedPhotos(values);
                }));
        contextMenu.addItem(modifyGis);
    }

    private void addModifyDateMenu(Menu contextMenu) {
        MenuItem modifyDate = new MenuItem("修改时间");
        modifyDate.addClickHandler(menuItemClickEvent -> SC.askforValue("请输入日期 yyyy-MM-dd", s -> {
            HashMap values = new HashMap();
            values.put("takenDate", s);
            ((PicsGrid) getTileGrid()).updateSelectedPhotos(values);
        }));
        contextMenu.addItem(modifyDate);
    }

    @Override
    public String getInnerHTML() {
        TileRecord record = getRecord();
        if (record == null) {
            return "";
        }
        // SC.logWarn("getInnerHTML......" + record.getAttribute("name")+ " " + record.getAttribute("mediaType"));

        if ("video".equals(record.getAttribute("mediaType"))) {
            String duration = record.getAttribute("duration");
            if (duration != null) {
                double v = Double.parseDouble(duration);
                if (durationLabel == null) {
                    durationLabel = new Label(NumberFormat.getFormat("#.00s").format(v));
                    addChild(durationLabel);
                }
                durationLabel.setVisible(true);
                durationLabel.setIcon("start.png");
                durationLabel.setHeight(10);
                durationLabel.setWidth(30);
                durationLabel.setBackgroundColor("white");
            }
        } else if (durationLabel != null) {
            durationLabel.setVisible(false);
        }
        String innerHTML = super.getInnerHTML();
        Integer rotate = record.getAttributeAsInt("rotate");
        if (rotate.equals(0)) {
            return innerHTML;
        }

        return innerHTML.replace("img src", "img style='transform:rotate(" + rotate + "deg);' src");
    }

    @Override
    public Canvas getHoverComponent() {
        return new Label(getHoverString(getRecord(), true));
    }
}
