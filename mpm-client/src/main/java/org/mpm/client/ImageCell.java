package org.mpm.client;

import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValueIconMapper;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;

@BeanFactory.Generate
public class ImageCell extends DynamicForm {

    public ImageCell() {
        super();
        setOverflow(Overflow.HIDDEN);
        setHeight(150);
        setWidth(200);
        setAutoChildVisibility("title", false);

        StaticTextItem pictureItem = new StaticTextItem("name");
        pictureItem.setRowSpan(3);
        pictureItem.setShowTitle(false);
        pictureItem.setCanEdit(false);
        pictureItem.setImageURLPrefix(ServerConfig.thumbUrl);

        pictureItem.setImageURLSuffix("/thumb");
        pictureItem.setShowValueIconOnly(true);
        pictureItem.setValueIconHeight(148);
        pictureItem.setValueIconWidth(120);
        pictureItem.setValueIconMapper(new ValueIconMapper() {
            @Override
            public String getValueIcon(Object value) {
                return String.valueOf(value);
            }
        });

        StaticTextItem title = new StaticTextItem("title");
        title.setVisible(false);
        setFields(pictureItem, title);
    }
}
