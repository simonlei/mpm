package org.mpm.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.PageKeyHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import java.util.ArrayList;
import java.util.List;
import org.mpm.client.util.Utils;

public class PicsGrid extends TileGrid {

    private final Criteria criteria;

    public PicsGrid() {
        super();
        criteria = new Criteria();
        criteria.addCriteria("trashed", false);

        setInitialCriteria(criteria);

        Page.registerKey("D", new PageKeyHandler() {
            @Override
            public void execute(String s) {
                trashSelectedPics();
            }
        });

        GWT.create(ImageCellMetaFactory.class);
        RestDataSource dataSource = Utils.createDataSource("pics", "/pics/fetch");
        dataSource.addField(new DataSourceTextField("name"));
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
    }

    private void trashSelectedPics() {
        // TODO: record the action to undo
        List<Long> ids = new ArrayList<>();
        Record[] selection = getSelection();
        for (Record r : selection) {
            ids.add(r.getAttributeAsLong("id"));
            SC.logWarn("Trash selected:" + r.getAttribute("id"));
        }
        RPCRequest switchTrashRequest = Utils.makeRPCRequest("/pics/switchTrash", "ids", ids);
        RPCManager.sendRequest(switchTrashRequest, (rpcResponse, o, rpcRequest) -> {
            SC.logWarn("Trashed."); // to reload.
            fetchData(criteria);
            markForRedraw();
        });
    }


    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

}
