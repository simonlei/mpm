package org.mpm.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import org.mpm.client.events.PicsChangeEvent;

public class PicsGrid extends TileGrid {

    static PicsGrid instance;
    private boolean trashed = false;
    private SinglePhotoDialog singlePhotoDialog;

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
        /*
        RestDataSource dataSource = ClientUtils.createDataSource("pics", "/pics/fetch");
        dataSource.setRemoveDataURL("/pics/remove");
        DataSourceSequenceField id = new DataSourceSequenceField("id");
        id.setPrimaryKey(true);
        dataSource.addField(id);
        dataSource.addField(new DataSourceTextField("name"));
        setDataSource(dataSource);

         */
        addDataArrivedHandler(dataArrivedEvent -> {
            fireChangeEvent(getResultSet().getLength());
        });

        setAutoFetchTextMatchStyle(TextMatchStyle.EXACT);
        DataSource dataSource = DataSource.get("pics");
        dataSource.addDataChangedHandler(dataChangedEvent -> {
            fireChangeEvent(getResultSet().getLength());
            LeftTabSet.instance.reloadData(trashed);
        });
        setDataSource(dataSource);
        setAutoFetchData(true);
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
        setInitialCriteria(criteria);
        // dataSource.fetchData(criteria);
    }

    public static boolean isTrashed() {
        return instance.trashed;
    }

    public static void setTrashed(boolean trashed) {
        instance.trashed = trashed;
        instance.reloadData();
        LeftTabSet.instance.reloadData(trashed);
    }

    private void fireChangeEvent(int length) {
        PhotoManagerEntryPoint.eventBus.fireEvent(new PicsChangeEvent(length));
    }

    private void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", trashed);
        // setInitialCriteria(criteria);
        fetchData(criteria);
        // setImplicitCriteria(criteria);
//        getDataSource().fetchData(criteria);
//        getDataSource().filterData(criteria);

        // getDataSource()
//        markForRedraw();
//        setImplicitCriteria(criteria);
//        invalidateCache();
    }

    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

}
