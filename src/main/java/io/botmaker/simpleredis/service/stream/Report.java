package io.botmaker.simpleredis.service.stream;

public final class Report {

    final StringBuilder problemsBuffer = new StringBuilder(1000);
    public int problemsCounter;
    public int successfulCounter;
    long startingTime;
    long endingTime;

    public boolean hasAnyProblems() {
        return problemsCounter > 0;
    }

    public synchronized void addSuccessful() {
        successfulCounter++;
    }

    public int getTotal() {
        return successfulCounter + problemsCounter;
    }

    public synchronized void addProblems(final String problem) {
        if (problemsBuffer.length() < 10000) {
            problemsBuffer.append(problem).append("\n\n");
        }
        problemsCounter++;
    }

    public String getExecutionReport() {
        final int total = getTotal();
        String itemsPerSec = "N/A";

        if (startingTime != 0L && endingTime != 0L) {
            itemsPerSec = Integer.toString((int) (total / (((endingTime - startingTime) / 1000f))));
        }

        return "[" + (problemsCounter == 0 ? "No " : problemsCounter) + "] problems found " +
                "when processing [" + total + "] items. " +
                "[" + itemsPerSec + "] items per sec. " +
                "processing time was [" + getRunningTime() + "] secs\n" +
                problemsBuffer;
    }

    public String getProblemsReport() {
        return problemsBuffer.toString();
    }

    public String getRunningTime() {
        if (startingTime == 0L || endingTime == 0L) {
            return "N/A";
        }
        return Integer.toString(((int) ((endingTime - startingTime) / 1000f)));
    }
}
