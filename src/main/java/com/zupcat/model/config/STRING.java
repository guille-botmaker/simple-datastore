package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;

public final class STRING extends AbstractPropertyBuilder<StringProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public STRING(final DatastoreEntity owner) {
        super(new StringProperty(owner));
    }
}
