package org.mpm.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Window;
import org.mpm.client.events.PicsChangeEvent;


public class SinglePhotoDialog extends Window {

    PicsGrid picsGrid;
    Record record;
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

    private void showPhoto(int inc) {
        int nextIndex = picsGrid.getRecordIndex(record) + inc;
        if (nextIndex < 0) { // first one, do nothing.
            return;
        }
        int totalCount = picsGrid.getResultSet().getLength();
        if (nextIndex > totalCount - 1) { // last one, do nothing.
            return;
        }
        setPhoto(picsGrid.getTileRecord(picsGrid.getTile(nextIndex)));
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
        if (this.record != null
                && record.getAttribute("name").equals(this.record.getAttribute("name"))) {
            return;
        }
        this.record = record;
        picsGrid.deselectAllRecords();
        picsGrid.selectRecord(record);

        SC.logWarn("Name :" + record.getAttribute("name") + pane.getCanFocus());
        scale = true;
        setPaneContent();

        pane.redraw();
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
            pane.setContents(
                    (scale
                            ? "<style> img {  max-width: 100%;max-height: 100%; display: block;margin: 0 auto;}</style> "
                            : "<style> img {  display: block;margin: 0 auto;}</style> ")
                            + "<img src=\""
                            + ServerConfig.thumbUrl + record.getAttribute("name") + "\""
                            + " title=\"" + ImageCell.getHoverString(record, false) + "\""
                            + "/>"
            );
        }
    }
}
