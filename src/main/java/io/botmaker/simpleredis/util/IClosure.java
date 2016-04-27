package io.botmaker.simpleredis.util;

/**
 * Handy closure class
 */
public interface IClosure {

    void execute(final Object params) throws Exception;
}
