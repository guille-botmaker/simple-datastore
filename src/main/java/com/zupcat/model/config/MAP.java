package com.zupcat.model.config;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.JSONObjectProperty;

public final class MAP<T extends DataObject> extends AbstractPropertyBuilder<JSONObjectProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP(final DatastoreEntity owner) {
        super(new JSONObjectProperty(owner), null);
    }
}
