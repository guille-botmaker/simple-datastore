package com.zupcat.audit;

/**
 * Callback called when an auditable object property is changed.
 * This default implementation does nothing. This behavior could be changed calling setDefaultImpl
 */
public final class AuditHandlerServiceFactory {

    private static AuditHandlerService defaultImpl = new AuditHandlerDefaultImpl();


    public static void setDefaultImpl(final AuditHandlerService _defaultImpl) {
        defaultImpl = _defaultImpl;
    }

    public static AuditHandlerService getAuditHandler() {
        return defaultImpl;
    }
}
