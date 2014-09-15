package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.LongProperty;

public final class LONG extends AbstractPropertyBuilder<LongProperty, Long> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LONG(final DatastoreEntity owner) {
        super(new LongProperty(owner), 0l);
    }
}
