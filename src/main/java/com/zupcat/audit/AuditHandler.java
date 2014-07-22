package com.zupcat.audit;

import com.zupcat.model.PropertyMeta;

public interface AuditHandler {

    void logPropertyDataChanged(final PropertyMeta property, final Object newValue, final String ownerId);
}
