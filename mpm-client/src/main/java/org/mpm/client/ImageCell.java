package org.mpm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IconButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.tile.SimpleTile;
import com.smartgwt.client.widgets.tile.TileRecord;
import java.util.Date;
import java.util.HashMap;
import org.mpm.client.actions.RotateMenuItem;

@BeanFactory.Generate
public class ImageCell extends SimpleTile {

    IconButton starButton = new IconButton();
    HLayout icons = new HLayout();
    Label durationLabel = new Label();

    public ImageCell() {
        super();
        setOverflow(Overflow.HIDDEN);
        setShowSelectedIcon(true);
        setShowHoverComponents(true);
        setHoverWidth(300);
        setHoverAutoFitWidth(true);
        Menu contextMenu = new Menu();
        addModifyDateMenu(contextMenu);
        addCopyGisMenu(contextMenu);
        addModifyGisMenu(contextMenu);
        addModifyDescMenu(contextMenu);
        addRotateMenu(contextMenu);
        setContextMenu(contextMenu);

        icons.setHeight(24);
        icons.addMember(starButton);
        icons.addMember(durationLabel);
        addChild(icons);
        durationLabel.setIcon("start.png");
        durationLabel.setAutoFit(true);
        durationLabel.setBackgroundColor("white");
        durationLabel.setVisible(false);
        starButton.setShowButtonTitle(false);
        starButton.setWidth(24);
        starButton.addClickHandler(clickEvent -> {
            HashMap values = new HashMap();
            values.put("star", !getRecord().getAttributeAsBoolean("star"));
            ((PicsGrid) getTileGrid()).updateSelectedPhotos(values, false);
        });
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

    static void addRotateMenu(Menu contextMenu) {
        MenuItem rotateMenuItem = new MenuItem("旋转");

        Menu rotateMenu = new Menu();
        rotateMenu.addItem(new RotateMenuItem("左旋90度", -90));
        rotateMenu.addItem(new RotateMenuItem("右旋90度", 90));
        rotateMenu.addItem(new RotateMenuItem("旋转180度", 180));
        rotateMenu.addItem(new RotateMenuItem("重置", 0));
        rotateMenuItem.setSubmenu(rotateMenu);
        contextMenu.addItem(rotateMenuItem);
    }

    private static native void copyToClipBoard(final String text) /*-{
      $wnd.navigator.clipboard.writeText(text);
    }-*/;

    @Override
    public String getBaseStyle() {
        String baseStyle = super.getBaseStyle();
        SC.logWarn("Base style " + baseStyle);
        return baseStyle;
    }

    private void addModifyDescMenu(Menu contextMenu) {
        MenuItem modifyDescItem = new MenuItem("修改描述信息");
        modifyDescItem.addClickHandler(
                menuItemClickEvent -> SC.askforValue("修改描述信息", "请输入描述信息",
                        getRecord().getAttribute("description"),
                        s -> {
                            if (s != null && s.trim().length() > 0) {
                                HashMap values = new HashMap();
                                values.put("description", s);
                                ((PicsGrid) getTileGrid()).updateSelectedPhotos(values, false);
                            }
                        }, new Dialog()));
        contextMenu.addItem(modifyDescItem);
    }

    private void addCopyGisMenu(Menu contextMenu) {
        MenuItem copyGis = new MenuItem("拷贝GIS信息");
        copyGis.addClickHandler(menuItemClickEvent -> {
            String gis = getRecord().getAttributeAsString("longitude") + ","
                    + getRecord().getAttributeAsString("latitude");
            copyToClipBoard(gis);
        });
        contextMenu.addItem(copyGis);
    }

    private void addModifyGisMenu(Menu contextMenu) {
        MenuItem modifyGis = new MenuItem("修改位置信息");
        modifyGis.addClickHandler(
                menuItemClickEvent -> SC.askforValue("请输入Gis信息， 经度,纬度", s -> {
                    if (s != null && s.trim().length() > 0) {
                        HashMap values = new HashMap();
                        String[] strs = s.split(",");
                        values.put("longitude", strs[0]);
                        values.put("latitude", strs[1]);
                        ((PicsGrid) getTileGrid()).updateSelectedPhotos(values, false);
                    }
                }));
        contextMenu.addItem(modifyGis);
    }

    private void addModifyDateMenu(Menu contextMenu) {
        MenuItem modifyDate = new MenuItem("修改时间");
        modifyDate.addClickHandler(menuItemClickEvent -> SC.askforValue("请输入日期 yyyy-MM-dd", s -> {
            if (s != null && s.trim().length() > 0) {
                HashMap values = new HashMap();
                values.put("takenDate", s);
                ((PicsGrid) getTileGrid()).updateSelectedPhotos(values, true);
            }
        }));
        contextMenu.addItem(modifyDate);
    }

    @Override
    public String getInnerHTML() {
        TileRecord record = getRecord();
        if (record == null) {
            return "";
        }
        starButton.setIcon(record.getAttributeAsBoolean("star") ? "star.png" : "notstar.png");
        durationLabel.setVisible(false);

        if ("video".equals(record.getAttribute("mediaType"))) {
            String duration = record.getAttribute("duration");
            if (duration != null) {
                double v = Double.parseDouble(duration);
                durationLabel.setContents(NumberFormat.getFormat("#.00s").format(v));
                durationLabel.setVisible(true);
            }
        }
        return super.getInnerHTML();
        /*
        String innerHTML = super.getInnerHTML();
        Integer rotate = record.getAttributeAsInt("rotate");
        if (rotate == null || rotate.equals(0)) {
            return innerHTML;
        }
        return innerHTML.replace("img src", "img style='transform:rotate(" + rotate + "deg);' src");

         */
    }

    @Override
    public Canvas getHoverComponent() {
        return new Label(getHoverString(getRecord(), true));
    }
}
