package com.zupcat.model.config;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.DataObjectProperty;

public final class DATA_OBJECT<T extends DataObject> extends AbstractPropertyBuilder<DataObjectProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public DATA_OBJECT(final DatastoreEntity owner, final Class<T> itemClass) {
        super(new DataObjectProperty(owner, itemClass), null);
    }
}
