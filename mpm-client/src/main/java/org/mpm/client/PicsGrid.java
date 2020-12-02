package org.mpm.client;

import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.widgets.tile.TileGrid;
import org.mpm.client.util.Utils;

public class PicsGrid extends TileGrid {

    public PicsGrid() {
        super();
        
        RestDataSource dataSource = Utils.createDataSource("pics", "/pics/fetch");
        setDataSource(dataSource);
        setAutoFetchData(true);
    }

}
