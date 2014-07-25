package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.util.IClosure;
import com.zupcat.util.RetryingExecutor;

public class RetryingExecutorTest extends AbstractTest {

    public void testFailure() {
        final int[] executions = new int[1];
        executions[0] = 0;

        final RetryingExecutor retryingExecutor = new RetryingExecutor(10, 200, new IClosure() {
            @Override
            public void execute(final Object params) throws Exception {
                assertEquals(params, "hi");

                executions[0] = executions[0] + 1;

                throw new RuntimeException("some kind of fake problems");
            }
        }, "hi");

        long time = 0;

        try {
            time = System.currentTimeMillis();
            retryingExecutor.startExecution();

        } catch (final Exception _exception) {
            time = System.currentTimeMillis() - time;

            assertEquals(executions[0], 10);
            assertTrue(time >= (160 * 10));
        }
    }

    public void testOk() throws Exception {
        final RetryingExecutor retryingExecutor = new RetryingExecutor(10, 200, new IClosure() {
            @Override
            public void execute(final Object params) throws Exception {
                System.err.println("there");
            }
        }, "hi");

        retryingExecutor.startExecution();
    }
}
