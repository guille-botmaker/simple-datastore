package io.botmaker.simpleredis.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public abstract class CommonsParallel<T> {

    private final List<Future<Result<T>>> resultsList = Collections.synchronizedList(new ArrayList<>(5000));
    private final List<Result<T>> problemsList = new ArrayList<>(500);

    private final ExecutorService executorService;


    protected CommonsParallel(final int maxConcurrency) {
        executorService = Executors.newFixedThreadPool(maxConcurrency);
    }

    protected CommonsParallel(final int maxConcurrency, final ThreadFactory threadFactory) {
        executorService = Executors.newFixedThreadPool(maxConcurrency, threadFactory);
    }

    protected abstract void preExecution();

    protected void postExecution() throws Exception {
        // nothing to do
    }

    protected abstract void doWorkConcurrently(final T inputObject) throws Exception;


    protected void workOnThread(final T inputObject) {
        final Future<Result<T>> future = executorService.submit(() -> {
            final Result<T> result = new Result<>(inputObject);

            try {
                doWorkConcurrently(inputObject);
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
                result.setProblems(throwable);
            }
            return result;
        });
        resultsList.add(future);
    }


    public void start() throws Exception {
        preExecution();

        executorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException ie) {
            throw new RuntimeException("Problems on shutting down previous work: " + ie.getMessage(), ie);
        }

        try {
            for (final Future<Result<T>> resultFuture : resultsList) {
                final Result<T> result = resultFuture.get();

                if (!result.wasOk()) {
                    problemsList.add(result);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Problems on getting results: " + e.getMessage(), e);
        }
        resultsList.clear();

        postExecution();
    }


    public String getProblemsRepresentation() {
        final StringBuilder builder = new StringBuilder(1000);
        int i = 1;

        builder.append("Problems found:\n");

        for (final Result<T> result : getProblems()) {
            final Throwable problems = result.getProblems();

            builder.append("[");
            builder.append(i);
            builder.append("] for input objects [");
            builder.append(result.getInputObject());
            builder.append("] problems: ");
            builder.append(problems.getMessage());
            builder.append("\n");
            builder.append("{\n");

            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            problems.printStackTrace(printWriter);
            printWriter.flush();

            builder.append(stringWriter.toString());
            builder.append("}\n");
            i++;
        }
        return builder.toString();
    }


    public boolean hasProblems() {
        return !getProblems().isEmpty();
    }

    public List<Result<T>> getProblems() {
        return problemsList;
    }


    public static final class Result<T> {

        private final T inputObject;
        private Throwable throwable = null;


        public Result(final T _inputObject) {
            inputObject = _inputObject;
        }

        public T getInputObject() {
            return inputObject;
        }

        public Throwable getProblems() {
            return throwable;
        }

        public void setProblems(final Throwable throwable) {
            this.throwable = throwable;
        }

        public boolean wasOk() {
            return throwable == null;
        }
    }
}
