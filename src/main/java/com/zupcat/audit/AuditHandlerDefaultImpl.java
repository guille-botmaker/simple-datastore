package com.zupcat.audit;

import com.zupcat.model.PropertyMeta;

/**
 * Callback called when an auditable object property is changed.
 * This default implementation does nothing
 */
public final class AuditHandlerDefaultImpl implements AuditHandlerService {

    public void logPropertyDataChanged(final PropertyMeta property, final Object newValue, final String ownerId) {
        // nothing to do
    }
}
