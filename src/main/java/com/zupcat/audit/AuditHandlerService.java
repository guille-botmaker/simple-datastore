package com.zupcat.audit;

public final class AuditHandlerService {

    private static AuditHandler defaultImpl = new AuditHandlerDefaultImpl();


    public static void setDefaultImpl(final AuditHandler _defaultImpl) {
        defaultImpl = _defaultImpl;
    }

    public static AuditHandler getAuditHandler() {
        return defaultImpl;
    }
}
