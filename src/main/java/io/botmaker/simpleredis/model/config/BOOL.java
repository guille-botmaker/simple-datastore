package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.DatastoreEntity;
import io.botmaker.simpleredis.property.BooleanProperty;

public final class BOOL extends AbstractPropertyBuilder<BooleanProperty, Boolean> {

    private static final long serialVersionUID = -2702019046191004750L;


    public BOOL(final DatastoreEntity owner) {
        super(new BooleanProperty(owner), false);
    }
}
