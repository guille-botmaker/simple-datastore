package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ListProperty;

public final class LIST<T> extends AbstractPropertyBuilder<ListProperty<T>, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST(final DatastoreEntity owner) {
        super(new ListProperty<T>(owner), null);
    }
}
