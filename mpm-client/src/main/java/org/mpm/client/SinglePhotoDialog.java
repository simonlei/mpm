package org.mpm.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.types.ReadOnlyDisplayAppearance;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.IconButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.DateItem;
import com.smartgwt.client.widgets.form.fields.IntegerItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.SplitPane;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import java.util.HashMap;
import org.mpm.client.events.PicsCountChangeEvent;


public class SinglePhotoDialog extends Window {

    PicsGrid picsGrid;
    Record record;
    IconButton starButton;
    HTMLPane pane = new HTMLPane();
    DynamicForm propertiesPane = new DynamicForm();
    private boolean show = false;
    private boolean scale = true;

    public SinglePhotoDialog(PicsGrid picsGrid) {
        setShowHeader(false);
        setIsModal(true);

        setWidth100();
        setHeight100();
        this.picsGrid = picsGrid;

        pane.setWidth100();
        pane.setHeight100();
        this.setAlwaysShowScrollbars(false);
        pane.setAlwaysShowScrollbars(false);
        Menu menu = new Menu();
        ImageCell.addRotateMenu(menu);
        pane.setContextMenu(menu);

        starButton = new IconButton();
        starButton.setShowButtonTitle(false);
        starButton.setZIndex(1000000);

        starButton.addClickHandler(clickEvent -> {
            swapStar();
        });

        SplitPane contentPane = new SplitPane();
        contentPane.setWidth100();
        contentPane.setHeight100();
        contentPane.setShowListToolStrip(false);

        contentPane.setNavigationPane(pane);
        contentPane.setNavigationPaneWidth("90%");

        VLayout detailPane = new VLayout();
        initPropertiesPane(picsGrid);

        detailPane.addMember(propertiesPane);
        contentPane.setDetailPane(detailPane);
        contentPane.setShowDetailToolStrip(false);

        addChild(starButton);
        addChild(contentPane);
        hide();
    }

    private void initPropertiesPane(PicsGrid picsGrid) {
        propertiesPane.setIsGroup(true);
        propertiesPane.setGroupTitle("属性");
        propertiesPane.setNumCols(1);
        propertiesPane.setDataSource(picsGrid.getDataSource());
        DateItem takenDateItem = new DateItem("takenDate");
        takenDateItem.setTitle("时间");
        TextItem addressItem = new TextItem("address");
        addressItem.setTitle("地点");
        TextAreaItem descItem = new TextAreaItem("description");
        descItem.setTitle("描述");
        IntegerItem heightItem = new IntegerItem("height");
        heightItem.setTitle("高");
        heightItem.setCanEdit(false);
        heightItem.setReadOnlyDisplay(ReadOnlyDisplayAppearance.STATIC);
        IntegerItem widthItem = new IntegerItem("width");
        widthItem.setTitle("宽");
        widthItem.setCanEdit(false);
        widthItem.setReadOnlyDisplay(ReadOnlyDisplayAppearance.STATIC);

        SubmitItem submitItem = new SubmitItem("保存");

        propertiesPane.setFields(takenDateItem, addressItem, descItem, heightItem, widthItem, submitItem);
    }

    private void swapStar() {
        HashMap values = new HashMap();
        boolean newValue = !record.getAttributeAsBoolean("star");
        values.put("star", newValue);
        picsGrid.updateSelectedPhotos(values, false);
        starButton.setIcon(newValue ? "star.png" : "notstar.png");
    }

    void showPhoto(int inc) {
        int nextIndex = picsGrid.getRecordIndex(record) + inc;
        if (nextIndex < 0) { // first one, do nothing.
            return;
        }
        int totalCount = picsGrid.getResultSet().getLength();
        if (nextIndex > totalCount - 1) { // last one, do nothing.
            return;
        }
        setPhoto(picsGrid.getResultSet().get(nextIndex));
    }

    public boolean isShow() {
        return show;
    }

    void hideMe() {
        if (record != null) {
            picsGrid.selectRecord(record);
        }
        show = false;
        record = null;
        hide();
    }

    public void setPhoto(Record record) {
//        if (this.record != null
//                && record.getAttribute("name").equals(this.record.getAttribute("name"))) {
//            return;
//        }
        this.record = record;
        picsGrid.deselectAllRecords();
        picsGrid.selectRecord(record);

        SC.logWarn("Name :" + record.getAttribute("name") + pane.getCanFocus());
        scale = true;
        setPaneContent();

        pane.redraw();
        starButton.setIcon(record.getAttributeAsBoolean("star") ? "star.png" : "notstar.png");
        propertiesPane.reset();
        propertiesPane.editRecord(record);

        show = true;
        show();
    }

    private void setPaneContent() {
        if ("video".equals(record.getAttribute("mediaType"))) {
            String videoUrl = ServerConfig.baseUrl + "video/" + record.getAttribute("name");
            pane.setContents("<video\n"
                    + "    id=\"my-player\"\n"
                    + "    class=\"video-js vjs-big-play-centered vjs-fill\""
                    + "    controls\n"
                    + "    preload=\"auto\"\n"
                    + "    poster=\"" + ServerConfig.baseUrl + record.getAttribute("name")
                    + "\"\n"
                    + "    data-setup='{}'>\n"
                    + "  <source src=\"" + videoUrl
                    + "\" type=\"video/mp4\"></source>\n"
                    + "  <source src=\"" + videoUrl
                    + "\" type=\"video/webm\"></source>\n"
                    + "  <source src=\"" + videoUrl
                    + "\" type=\"video/ogg\"></source>\n"
                    + "  <p class=\"vjs-no-js\">\n"
                    + "    To view this video please enable JavaScript, and consider upgrading to a\n"
                    + "    web browser that\n"
                    + "    <a href=\"https://videojs.com/html5-video-support/\" target=\"_blank\">\n"
                    + "      supports HTML5 video\n"
                    + "    </a>\n"
                    + "  </p>\n"
                    + "</video>");
        } else {
            int rotate = record.getAttributeAsInt("rotate");
            pane.setContents(
                    "<img style=\""
                            + (scale
                            ? "max-width: 100%;max-height: 100%; display: block;margin: 0 auto; transform: rotate("
                            + rotate + "deg);"
                            : "display: block;margin: 0 auto;transform: rotate(" + rotate + "deg);")
                            + "\" src=\""
                            + ServerConfig.baseUrl + record.getAttribute("name") + "\""
                            + " title=\"" + ImageCell.getHoverString(record, false) + "\""
                            + "/>"
            );
        }
        propertiesPane.editRecord(record);
    }

    public void rotateCurrentRecord(int rotate) {
        if (record == null) {
            return;
        }
        if (rotate == 0) {
            record.setAttribute("rotate", 0);
        } else {
            record.setAttribute("rotate",
                    Utils.getDegree(record.getAttributeAsInt("rotate") + rotate));
        }
        PicsGrid.instance.getDataSource().updateData(record);
        setPaneContent();
        pane.redraw();
    }

    public void deleteCurrentPhoto() {
        if (record != null) {
            int index = picsGrid.getRecordIndex(record);
            SC.logWarn("Delete current..." + index);
            int totalCount = picsGrid.getResultSet().getLength();
            Record nextRecord;
            if (totalCount == 1) { // only one, exit
                nextRecord = null;
            } else if (index == totalCount - 1) { // last one, get prev one
                nextRecord = picsGrid.getTileRecord(picsGrid.getTile(index - 1));
            } else { // get next one
                nextRecord = picsGrid.getTileRecord(picsGrid.getTile(index + 1));
            }
            picsGrid.removeData(record);
            PhotoManagerEntryPoint.eventBus.fireEvent(new PicsCountChangeEvent(totalCount - 1));
            if (nextRecord == null) {
                hideMe();
            } else {
                setPhoto(nextRecord);
            }
        }
    }

    class SinglePageKeyHandler extends PageKeyHandler {

        @Override
        public void execute(String s) {
            if (!isShow() || record == null || EventHandler.ctrlKeyDown()
                    || EventHandler.altKeyDown() || EventHandler.shiftKeyDown()) {
                return;
            }
            switch (s) {
                case KeyNames.ESC:
                    hideMe();
                    break;
                case KeyNames.ARROW_LEFT:
                    showPhoto(-1);
                    break;
                case KeyNames.ARROW_RIGHT:
                case KeyNames.SPACE:
                    showPhoto(1);
                    break;
                case "I":
                    scale = !scale;
                    setPaneContent();
                    break;
                case "S":
                    swapStar();
                    break;
                case "J":
                    pane.scrollByPercent(0, 50);
                    break;
                case "K":
                    pane.scrollByPercent(0, -50);
                    break;
                case "L":
                    pane.scrollByPercent(50, 0);
                    break;
                case "H":
                    pane.scrollByPercent(-50, 0);
                    break;
                case "D":
                    deleteCurrentPhoto();
                    break;
                case "R":
                    rotateCurrentRecord(90);
                    break;
            }
        }
    }
}
