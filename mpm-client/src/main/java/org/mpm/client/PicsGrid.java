package org.mpm.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceSequenceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import java.util.ArrayList;
import java.util.List;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.util.ClientUtils;

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
                    PhotoManagerEntryPoint.eventBus
                            .fireEvent(new PicsChangeEvent(getResultSet().getLength() - count));
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
        RestDataSource dataSource = ClientUtils.createDataSource("pics", "/pics/fetch");
        dataSource.setRemoveDataURL("/pics/remove");
        DataSourceSequenceField id = new DataSourceSequenceField("id");
        id.setPrimaryKey(true);
        dataSource.addField(id);
        dataSource.addField(new DataSourceTextField("name"));
        setDataSource(dataSource);
        addDataArrivedHandler(dataArrivedEvent -> {
            PhotoManagerEntryPoint.eventBus
                    .fireEvent(new PicsChangeEvent(getResultSet().getLength()));
        });
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
        dataSource.fetchData(criteria);
    }

    public static boolean isTrashed() {
        return instance.trashed;
    }

    public static void setTrashed(boolean trashed) {
        instance.trashed = trashed;
        instance.reloadData();
    }

    private void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", trashed);
        // setInitialCriteria(criteria);
        setImplicitCriteria(criteria);
//        getDataSource().fetchData(criteria);
//        getDataSource().filterData(criteria);

        // getDataSource()
//        markForRedraw();
//        setImplicitCriteria(criteria);
//        invalidateCache();
    }

    private void trashSelectedPics() {
        removeSelectedData();
        // TODO: record the action to undo
        List<Long> ids = new ArrayList<>();
        Record[] selection = getSelection();
        for (Record r : selection) {
            ids.add(r.getAttributeAsLong("id"));
            SC.logWarn("Trash selected:" + r.getAttribute("id"));
        }
        RPCRequest switchTrashRequest = ClientUtils.makeRPCRequest("/pics/switchTrash", "ids", ids);
        RPCManager.sendRequest(switchTrashRequest, (rpcResponse, o, rpcRequest) -> {
            SC.logWarn("Trashed."); // to reload.
            // fetchData();
            // markForRedraw();
            // invalidateCache();
        });
    }


    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

}
