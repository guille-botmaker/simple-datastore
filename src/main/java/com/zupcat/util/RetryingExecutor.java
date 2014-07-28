package com.zupcat.util;

import com.zupcat.dao.RetryingHandler;
import com.zupcat.exception.NoMoreRetriesException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that encapsulates a retrying algorithm. Usefull for AppEngine API calls
 */
public final class RetryingExecutor {

    private static final Logger log = Logger.getLogger(RetryingExecutor.class.getName());

    private int maxRetries;
    private final int timeBetweenRetries;
    private final IClosure closure;
    private final Object params;

    public RetryingExecutor(final IClosure closure, final Object params) {
        this(4, 800, closure, params);
    }

    public RetryingExecutor(final int maxRetries, final int timeBetweenRetries, final IClosure closure, final Object params) {
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
