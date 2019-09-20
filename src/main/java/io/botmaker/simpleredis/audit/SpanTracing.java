package io.botmaker.simpleredis.audit;

public interface SpanTracing {

    Object startSpan(final String info);

    void endSpan(final Object span);
}
