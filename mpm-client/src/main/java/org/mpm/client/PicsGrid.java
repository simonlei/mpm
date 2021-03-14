package org.mpm.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.header.HeaderPanel;

public class PicsGrid extends TileGrid {

    static PicsGrid instance;
    private boolean trashed = false;
    private SinglePhotoDialog singlePhotoDialog;
    private int lastTxnNum = 0;

    public PicsGrid() {
        super();
        instance = this;
        singlePhotoDialog = new SinglePhotoDialog(this);
        Page.registerKey("D", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                int count = getSelection().length;
                if (!singlePhotoDialog.isShow() && count > 0) {
                    SC.logWarn("Delete tile...");
                    removeSelectedData();
                    // fireChangeEvent(getResultSet().getLength() - count);
                }
            }
        });
        Page.registerKey(KeyNames.ENTER, new PageKeyHandler() {
            @Override
            public void execute(String s) {
                singlePhotoDialog.setPhoto(instance.getSelectedRecord());
            }
        });
        addRecordDoubleClickHandler(recordDoubleClickEvent -> {
            singlePhotoDialog.setPhoto(recordDoubleClickEvent.getRecord());
        });

        GWT.create(ImageCellMetaFactory.class);
        addDataArrivedHandler(dataArrivedEvent -> {
            fireChangeEvent(getResultSet().getLength());
        });

        setAutoFetchTextMatchStyle(TextMatchStyle.EXACT);
        DataSource dataSource = DataSource.get("pics");
        dataSource.addDataChangedHandler(dataChangedEvent -> {
            int txnNum = dataChangedEvent.getDsResponse().getTransactionNum();
            if (txnNum > lastTxnNum) {
                lastTxnNum = txnNum;
                fireChangeEvent(getResultSet().getLength());
                LeftTabSet.instance.reloadData();
            }
        });
        setDataSource(dataSource);
        setAutoFetchData(false);
        setTileWidth(200);
        setTileHeight(150);

        setTileConstructor(ImageCell.class.getName());
        setSelectionType(SelectionStyle.MULTIPLE);

        DetailViewerField imgField = new DetailViewerField("name");
        imgField.setType("image");
        imgField.setImageURLPrefix("/thumb/");

        setFields(imgField);

        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        // 默认按id逆序
        fetchData(criteria, null, makeDSRequest("id", false));
    }

    public static boolean isTrashed() {
        return instance.trashed;
    }

    public static void setTrashed(boolean trashed) {
        instance.trashed = trashed;
        instance.reloadData();
        LeftTabSet.instance.reloadData();
    }

    public static void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", instance.trashed);
        criteria.addCriteria(LeftTabSet.getCriteria());

        instance.getResultSet().setSort(HeaderPanel.getSortSpecifier());
        instance.getResultSet().setCriteria(criteria);

        // SC.logWarn("Will fetch: " + instance.getResultSet().willFetchData(criteria));
        instance.fetchData(criteria);
    }

    private static DSRequest makeDSRequest(String field, boolean asc) {
        DSRequest dsRequest = new DSRequest();
        dsRequest.setSortBy(new SortSpecifier[]{new SortSpecifier(field,
                asc ? SortDirection.ASCENDING : SortDirection.DESCENDING)});
        return dsRequest;
    }

    private void fireChangeEvent(int length) {
        PhotoManagerEntryPoint.eventBus.fireEvent(new PicsChangeEvent(length));
    }

    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

}
