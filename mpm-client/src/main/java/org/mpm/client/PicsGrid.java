package org.mpm.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceImageField;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.tile.TileGrid;
import org.mpm.client.util.Utils;

public class PicsGrid extends TileGrid {

    public PicsGrid() {
        super();
        GWT.create(ImageCellMetaFactory.class);
        RestDataSource dataSource = Utils.createDataSource("pics", "/pics/fetch");
        dataSource.addField(new DataSourceImageField("name"));
        setDataSource(dataSource);
        setAutoFetchData(true);
        setTileWidth(200);
        setTileHeight(150);
        setTileConstructor(ImageCell.class.getName());
        setSelectionType(SelectionStyle.MULTIPLE);

    }


    public interface ImageCellMetaFactory extends BeanFactory.MetaFactory {

        BeanFactory<ImageCell> getImageCellFactory();
    }

}
