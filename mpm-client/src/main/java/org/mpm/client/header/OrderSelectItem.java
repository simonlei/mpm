package org.mpm.client.header;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import java.util.LinkedHashMap;
import org.mpm.client.PicsGrid;

class OrderSelectItem extends SelectItem {

    public OrderSelectItem() {
        setWidth(90);
        this.setAttribute("editorType", "SelectItem");
        setShowTitle(false);
        LinkedHashMap<SortSpecifier, String> valueMap = new LinkedHashMap<>();
        SortSpecifier selectedValue = new SortSpecifier("id", SortDirection.DESCENDING);
        valueMap.put(selectedValue, "ID逆序");
        valueMap.put(new SortSpecifier("id", SortDirection.ASCENDING), "ID顺序");
        valueMap.put(new SortSpecifier("takenDate", SortDirection.DESCENDING), "时间逆序");
        valueMap.put(new SortSpecifier("takenDate", SortDirection.ASCENDING), "时间顺序");
        setValueMap(valueMap);
        setDefaultValue(selectedValue);

        addChangedHandler(changedEvent -> {
            SortSpecifier value = (SortSpecifier) getValue();

            PicsGrid.instance.sortByProperty(value.getField(),
                    value.getSortDirection() == SortDirection.ASCENDING);
            // SC.logWarn("Selected value : " + value.getField() + " : " + value.getSortDirection().getValue());
            PicsGrid.instance.reloadData();
        });
    }
}
