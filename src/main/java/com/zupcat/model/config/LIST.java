package com.zupcat.model.config;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ListProperty;

public final class LIST<T> extends AbstractPropertyBuilder<ListProperty<T>, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST(final DatastoreEntity owner) {
        this(owner, null);
    }

    public LIST(final DatastoreEntity owner, final Class<? extends DataObject> itemClass) {
        super(new ListProperty<T>(owner, itemClass), null);
    }
}
