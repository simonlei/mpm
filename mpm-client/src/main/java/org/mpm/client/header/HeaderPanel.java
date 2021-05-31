package org.mpm.client.header;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.PicsGrid;

public class HeaderPanel extends HLayout {

    static HeaderPanel instance;
    private final SwitchTrashButton switchTrashButton;
    private final SwitchStarButton switchStarButton;

    public HeaderPanel() {
        super();
        instance = this;
        ToolStrip toolStrip = new ToolStrip();

        SelectedIndexTextItem selectedIndexItem = new SelectedIndexTextItem();
        selectedIndexItem.setWidth(10);
        ToolStripButton jumpButton = new ToolStripButton("跳转");
        jumpButton.addClickHandler(clickEvent -> {
            PicsGrid.instance.deselectAllRecords();
            PicsGrid.instance.selectRecord(selectedIndexItem.getValueAsInteger() - 1);
            PicsGrid.instance.scrollTo();
        });
        toolStrip.addFormItem(selectedIndexItem);
        toolStrip.addButton(jumpButton);
        toolStrip.addSeparator();

        OrderSelectItem orderSelectItem = new OrderSelectItem();

        toolStrip.addFormItem(orderSelectItem);
        switchTrashButton = new SwitchTrashButton();
        toolStrip.addButton(switchTrashButton);
        switchStarButton = new SwitchStarButton();
        toolStrip.addButton(switchStarButton);

        toolStrip.addSeparator();
        toolStrip.addButton(new EmptyTrashButton());

        ImportButton formItem = new ImportButton();
        toolStrip.addButton(formItem);

        addMember(toolStrip);
    }

    public static Criteria getCriteria() {
        return instance._getCriteria();
    }

    public static boolean isTrashed() {
        return instance.switchTrashButton.isTrashed();
    }

    private Criteria _getCriteria() {
        Criteria criteria = new Criteria();
        criteria.addCriteria("trashed", instance.switchTrashButton.isTrashed());
        Criteria starCriteria = instance.switchStarButton.getCriteria();
        if (starCriteria != null) {
            criteria.addCriteria(starCriteria);
        }
        return criteria;
    }
}
