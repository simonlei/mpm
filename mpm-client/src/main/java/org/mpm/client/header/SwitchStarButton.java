package org.mpm.client.header;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.mpm.client.PicsGrid;

public class SwitchStarButton extends ToolStripButton {

    public SwitchStarButton() {
        super("只看star");
        setActionType(SelectionType.CHECKBOX);
        addClickHandler(clickEvent -> PicsGrid.staticReload());
    }

    public Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAttribute("star", true);
        return getSelected() ? criteria : null;
    }
}
