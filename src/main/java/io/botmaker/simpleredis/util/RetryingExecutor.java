package io.botmaker.simpleredis.util;

import io.botmaker.simpleredis.dao.RetryingHandler;
import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import org.apache.commons.collections4.Closure;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that encapsulates a retrying algorithm. Usefull for AppEngine API calls
 */
public final class RetryingExecutor<TPARAM> {

    private static final Logger log = Logger.getLogger(RetryingExecutor.class.getName());
    private final int timeBetweenRetries;
    private final Closure<TPARAM> closure;
    private final TPARAM params;
    private int maxRetries;

    public RetryingExecutor(final Closure<TPARAM> closure, final TPARAM params) {
        this(4, 800, closure, params);
    }

    public RetryingExecutor(final int maxRetries, final int timeBetweenRetries, final Closure<TPARAM> closure, final TPARAM params) {
        this.maxRetries = maxRetries;
        this.timeBetweenRetries = timeBetweenRetries;
        this.closure = closure;
        this.params = params;
    }

    public void startExecution() throws Exception {
        while (true) {
            try {
                closure.execute(params);
                break;
            } catch (final Exception exception) {
                handleError(exception);
            }
        }
    }

    private void handleError(final Exception exception) {
        maxRetries = maxRetries - 1;

        if (maxRetries == 0) {
            log.log(Level.SEVERE, "No more tries for closure [" + closure.getClass().getName() + "]: " + exception.getMessage(), exception);
            throw new NoMoreRetriesException(exception);
        }

        log.log(Level.INFO, "Retrying closure [" + closure.getClass().getName() + "]: " + exception.getMessage(), exception);

        RetryingHandler.sleep(timeBetweenRetries);
    }
}