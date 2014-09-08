package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ListStringProperty;

public final class LIST_STRING extends AbstractPropertyBuilder<ListStringProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST_STRING(final DatastoreEntity owner) {
        super(new ListStringProperty(owner));
    }
}
