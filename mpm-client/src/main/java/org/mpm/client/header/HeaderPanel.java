package org.mpm.client.header;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import org.mpm.client.ImportPhotoButton;

public class HeaderPanel extends HLayout {

    static HeaderPanel instance;
    SortSpecifier selectedValue;

    public HeaderPanel() {
        super();
        instance = this;
        ToolStrip toolStrip = new ToolStrip();
        OrderSelectItem orderSelectItem = new OrderSelectItem(this);

        toolStrip.addFormItem(orderSelectItem);
        toolStrip.addButton(new SwitchTrashButton());
        toolStrip.addButton(new EmptyTrashButton());

        toolStrip.addSeparator();
        toolStrip.addButton(new ImportPhotoButton());

        addMember(toolStrip);
    }

    public static SortSpecifier getSortSpecifier() {
        return instance.selectedValue;
    }

}
