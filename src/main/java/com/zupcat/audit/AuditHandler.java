package com.zupcat.audit;

import com.zupcat.model.PropertyMeta;

/**
 * Callback called when an auditable object property is changed.
 * A common usage is calling BigQuery to store that data
 */
public interface AuditHandler {

    void logPropertyDataChanged(final PropertyMeta property, final Object newValue, final String ownerId);
}
