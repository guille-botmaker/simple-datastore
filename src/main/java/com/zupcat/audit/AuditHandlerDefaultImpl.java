package com.zupcat.audit;

import com.zupcat.model.PropertyMeta;

public final class AuditHandlerDefaultImpl implements AuditHandler {

    public void logPropertyDataChanged(final PropertyMeta property, final Object newValue, final String ownerId) {
        // nothing to do
    }
}
