package org.mpm.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.IconButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.menu.Menu;
import java.util.HashMap;
import org.mpm.client.events.PicsChangeEvent;


public class SinglePhotoDialog extends Window {

    PicsGrid picsGrid;
    Record record;
    IconButton starButton;
    HTMLPane pane = new HTMLPane();
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

        addChild(starButton);

        addChild(pane);
        hide();
        Page.registerKey("D", new PageKeyHandler() {
            @Override
            public void execute(String s) {
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
                    // picsGrid.deselectAllRecords();
                    picsGrid.removeData(record);
                    PhotoManagerEntryPoint.eventBus.fireEvent(new PicsChangeEvent(totalCount - 1));
                    if (nextRecord == null) {
                        hideMe();
                    } else {
                        setPhoto(nextRecord);
                    }
                }
            }
        });
        Page.registerKey(KeyNames.ESC, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                hideMe();
            }
        });
        Page.registerKey(KeyNames.ARROW_LEFT, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                showPhoto(-1);
            }
        });
        Page.registerKey(KeyNames.ARROW_RIGHT, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                showPhoto(1);
            }
        });
        Page.registerKey(KeyNames.SPACE, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                showPhoto(1);
            }
        });
        Page.registerKey("I", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                scale = !scale;
                setPaneContent();
            }
        });
        Page.registerKey("S", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                swapStar();
            }
        });
        Page.registerKey("J", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                pane.scrollByPercent(0, 50);
            }
        });
        Page.registerKey("K", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                pane.scrollByPercent(0, -50);
            }
        });
        Page.registerKey("L", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                pane.scrollByPercent(50, 0);
            }
        });
        Page.registerKey("H", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                pane.scrollByPercent(-50, 0);
            }
        });

    }

    private void swapStar() {
        HashMap values = new HashMap();
        boolean newValue = !record.getAttributeAsBoolean("star");
        values.put("star", newValue);
        picsGrid.updateSelectedPhotos(values, false);
        starButton.setIcon(newValue ? "star.png" : "notstar.png");
    }

    private void showPhoto(int inc) {
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

    private void hideMe() {
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

        show = true;
        show();
    }

    private void setPaneContent() {
        if ("video".equals(record.getAttribute("mediaType"))) {
            String videoUrl = ServerConfig.thumbUrl + "video/" + record.getAttribute("name");
            pane.setContents("<video\n"
                    + "    id=\"my-player\"\n"
                    + "    class=\"video-js vjs-big-play-centered vjs-fill\""
                    + "    controls\n"
                    + "    preload=\"auto\"\n"
                    + "    poster=\"" + ServerConfig.thumbUrl + record.getAttribute("name")
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
                            + ServerConfig.thumbUrl + record.getAttribute("name") + "\""
                            + " title=\"" + ImageCell.getHoverString(record, false) + "\""
                            + "/>"
            );
        }
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
}
