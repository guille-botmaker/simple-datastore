package io.botmaker.simpleredis.audit;

public final class DefaultEmptySpanTracing implements SpanTracing {

    @Override
    public Object startSpan(final String info) {
        return null;
    }

    @Override
    public void endSpan(final Object span) {
    }
}
