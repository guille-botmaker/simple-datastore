package com.zupcat.audit;

/**
 * Callback called when an auditable object property is changed.
 * This default implementation does nothing. This behavior could be changed calling setDefaultImpl
 */
public final class AuditHandlerService {

    private static AuditHandler defaultImpl = new AuditHandlerDefaultImpl();


    public static void setDefaultImpl(final AuditHandler _defaultImpl) {
        defaultImpl = _defaultImpl;
    }

    public static AuditHandler getAuditHandler() {
        return defaultImpl;
    }
}
