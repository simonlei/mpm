package org.mpm.client;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import java.util.LinkedHashMap;

public class HeaderPanel extends HLayout {

    static HeaderPanel instance;
    SortSpecifier selectedValue;

    public HeaderPanel() {
        super();
        instance = this;
        ToolStrip toolStrip = new ToolStrip();
        OrderSelectItem orderSelectItem = new OrderSelectItem();

        toolStrip.addFormItem(orderSelectItem);
        toolStrip.addButton(new SwitchTrashButton());

        toolStrip.addSeparator();
        toolStrip.addButton(new ImportPhotoButton());

        addMember(toolStrip);
    }

    public static SortSpecifier getSortSpecifier() {
        return instance.selectedValue;
    }

    class OrderSelectItem extends SelectItem {

        public OrderSelectItem() {
            setWidth(100);
            setShowTitle(false);
            LinkedHashMap<SortSpecifier, String> valueMap = new LinkedHashMap<>();
            selectedValue = new SortSpecifier("id", SortDirection.DESCENDING);
            valueMap.put(selectedValue, "ID逆序");
            valueMap.put(new SortSpecifier("id", SortDirection.ASCENDING), "ID顺序");
            valueMap.put(new SortSpecifier("takenDate", SortDirection.DESCENDING), "时间逆序");
            valueMap.put(new SortSpecifier("takenDate", SortDirection.ASCENDING), "时间顺序");
            setValueMap(valueMap);
            setDefaultValue(selectedValue);

            addChangedHandler(changedEvent -> {
                selectedValue = (SortSpecifier) getValue();
                PicsGrid.reloadData();
            });
        }
    }
}
