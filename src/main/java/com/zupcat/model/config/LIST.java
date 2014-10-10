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
        this(owner, itemClass, false);
    }

    public LIST(final DatastoreEntity owner, final Class<? extends DataObject> itemClass, final boolean keepUniqueElements) {
        super(new ListProperty<T>(owner, itemClass, keepUniqueElements), null);
    }
}
