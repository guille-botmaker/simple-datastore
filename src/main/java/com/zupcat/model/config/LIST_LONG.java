package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ListLongProperty;

public final class LIST_LONG extends AbstractPropertyBuilder<ListLongProperty, Long> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST_LONG(final DatastoreEntity owner) {
        super(new ListLongProperty(owner));
    }
}
