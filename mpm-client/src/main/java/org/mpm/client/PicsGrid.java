package org.mpm.client;

import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.KeyNames;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import java.util.HashMap;
import java.util.Map;
import org.mpm.client.events.PicsChangeEvent;
import org.mpm.client.header.HeaderPanel;

public class PicsGrid extends TileGrid {

    public static PicsGrid instance;
    private SinglePhotoDialog singlePhotoDialog;
    private int lastTxnNum = 0;

    public PicsGrid() {
        super();
        instance = this;
        singlePhotoDialog = new SinglePhotoDialog(this);
        addRecordDoubleClickHandler(recordDoubleClickEvent -> {
            singlePhotoDialog.setPhoto(recordDoubleClickEvent.getRecord());
        });

        addDataArrivedHandler(dataArrivedEvent -> {
//            fireChangeEvent(getResultSet().getLength());
        });

        setAutoFetchTextMatchStyle(TextMatchStyle.EXACT);
        DataSource dataSource = DataSource.get("pics");
        dataSource.addDataChangedHandler(dataChangedEvent -> {
            DSOperationType operationType = dataChangedEvent.getDsRequest().getOperationType();
            int txnNum = dataChangedEvent.getDsResponse().getTransactionNum();
            if (DSOperationType.REMOVE.equals(operationType) && txnNum > lastTxnNum) {
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

        DetailViewerField imgField = new DetailViewerField("thumb");
        imgField.setType("image");
        // 直接在客户端拼接thumb地址就好了
        // imgField.setImageURLPrefix("/thumb/");
        imgField.setImageURLPrefix(ServerConfig.baseUrl);
        // imgField.setImageURLSuffix("/thumb");

        setFields(imgField);

        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", false);
        // 默认按id逆序
        sortByProperty("id", false);
        fetchData(criteria);
    }

    public static void reloadLeftAndPics() {
        instance.reloadData();
        LeftTabSet.instance.reloadData();
    }

    public static native void exportReloadData() /*-{
      $wnd.realodPicsGrid = $entry(@org.mpm.client.PicsGrid::reloadLeftAndPics());
    }-*/;

    public SinglePhotoDialog getSinglePhotoDialog() {
        return singlePhotoDialog;
    }

    public void updateSelectedPhotos(Map values, boolean needReload) {
        Record[] selection = getSelection();
        RPCManager.startQueue();
        for (Record r : selection) {
            for (Object k : values.keySet()) {
                if ("rotate".equals(k)) {
                    Integer rotate = (Integer) values.get(k);
                    if (rotate.equals(0)) {
                        r.setAttribute("rotate", 0);
                    } else {
                        r.setAttribute("rotate",
                                Utils.getDegree(r.getAttributeAsInt("rotate") + rotate));
                    }
                } else {
                    r.setAttribute((String) k, values.get(k));
                }
            }
            getDataSource().updateData(r);
        }
        RPCManager.sendQueue(rpcResponses -> {
            if (needReload) {
                reloadData();
            }
        });
    }

    public void reloadData() {
        Criteria criteria = new Criteria();
        criteria.addCriteria(HeaderPanel.getCriteria());
        criteria.addCriteria(LeftTabSet.getCriteria());

        SC.logWarn("Will fetch: " + getResultSet().willFetchData(criteria));

        invalidateCache();
        fetchData(criteria);
    }

    private void fireChangeEvent(int length) {
        PhotoManagerEntryPoint.eventBus.fireEvent(new PicsChangeEvent(length));
    }

    public void rotateSelectedPhotos(int degree) {
        HashMap values = new HashMap();
        values.put("rotate", degree);
        updateSelectedPhotos(values, false);
    }

    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

    class GridKeyHandler extends PageKeyHandler {

        @Override
        public void execute(String keyName) {
            if (singlePhotoDialog.isShow()) {
                return;
            }
            switch (keyName) {
                case KeyNames.ENTER:
                    singlePhotoDialog.setPhoto(getSelectedRecord());
                    break;
                case "D":
                    removeSelectedData();
                    break;
                case "R":
                    rotateSelectedPhotos(90);
                    break;
            }
        }
    }
}
