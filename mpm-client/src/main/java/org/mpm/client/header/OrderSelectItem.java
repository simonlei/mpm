package org.mpm.client.header;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import java.util.LinkedHashMap;
import org.mpm.client.PicsGrid;

class OrderSelectItem extends SelectItem {

    public OrderSelectItem(HeaderPanel headerPanel) {
        setWidth(90);
        setShowTitle(false);
        LinkedHashMap<SortSpecifier, String> valueMap = new LinkedHashMap<>();
        headerPanel.selectedValue = new SortSpecifier("id", SortDirection.DESCENDING);
        valueMap.put(headerPanel.selectedValue, "ID逆序");
        valueMap.put(new SortSpecifier("id", SortDirection.ASCENDING), "ID顺序");
        valueMap.put(new SortSpecifier("takenDate", SortDirection.DESCENDING), "时间逆序");
        valueMap.put(new SortSpecifier("takenDate", SortDirection.ASCENDING), "时间顺序");
        setValueMap(valueMap);
        setDefaultValue(headerPanel.selectedValue);

        addChangedHandler(changedEvent -> {
            headerPanel.selectedValue = (SortSpecifier) getValue();
            PicsGrid.reloadData();
        });
    }
}
