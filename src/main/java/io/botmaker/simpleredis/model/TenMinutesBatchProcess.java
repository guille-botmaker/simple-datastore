package io.botmaker.simpleredis.model;

import io.botmaker.simpleredis.model.config.INT;
import io.botmaker.simpleredis.model.config.LONG;
import io.botmaker.simpleredis.model.config.STRING;
import io.botmaker.simpleredis.property.IntegerProperty;
import io.botmaker.simpleredis.property.LongProperty;
import io.botmaker.simpleredis.property.StringProperty;
import io.botmaker.simpleredis.util.TimeUtils;

public final class TenMinutesBatchProcess extends RedisEntity {

    public LongProperty STARTING_TIME;
    public LongProperty ENDING_TIME;
    public IntegerProperty PROBLEMS_COUNTER;
    public IntegerProperty SUCCESSFUL_COUNTER;
    public StringProperty PROBLEMS_SAMPLE;

    private transient StringBuilder problemsBuffer;

    public TenMinutesBatchProcess() {
        this(null);
    }

    public TenMinutesBatchProcess(String isoTimestamp) {
        super(false, EXPIRING_1_MONTH);

        if (isoTimestamp == null) {
            isoTimestamp = TimeUtils.getCurrentAsISO();
        }
        setId(buildKeyFrom(isoTimestamp));
    }

    /**
     * @return if "2016-06-29T14:12:29.243Z" then "201606291410"
     */
    public static String buildKeyFrom(final String isoTimestamp) {
        return isoTimestamp.substring(0, 4) +
                isoTimestamp.substring(5, 7) +
                isoTimestamp.substring(8, 10) +
                isoTimestamp.substring(11, 13) +
                isoTimestamp.substring(14, 15) +
                "0";
    }

    @Override
    protected void config() {
        STARTING_TIME = new LONG(this).sendToClient().build();
        ENDING_TIME = new LONG(this).sendToClient().build();
        PROBLEMS_COUNTER = new INT(this).sendToClient().build();
        SUCCESSFUL_COUNTER = new INT(this).sendToClient().build();
        PROBLEMS_SAMPLE = new STRING(this).sendToClient().build();
    }

    public boolean hasAnyProblems() {
        return PROBLEMS_COUNTER.get() > 0;
    }

    public synchronized void addSuccessful() {
        SUCCESSFUL_COUNTER.increment();
    }

    public int getTotal() {
        return SUCCESSFUL_COUNTER.get() + PROBLEMS_COUNTER.get();
    }

    public synchronized void addProblems(final String problem) {
        if (problemsBuffer == null) {
            problemsBuffer = new StringBuilder(1000);
        }
        if (problemsBuffer.length() < 10000) {
            problemsBuffer.append(problem).append("\n\n");
        }
        PROBLEMS_COUNTER.increment();
    }

    public void markProcessFinished() {
        ENDING_TIME.set(System.currentTimeMillis());
        PROBLEMS_SAMPLE.set(problemsBuffer.toString());
        problemsBuffer = null;
    }

    public String getExecutionReport() {
        final int total = getTotal();
        String itemsPerSec = "N/A";
        final long startingTime = STARTING_TIME.get();
        final long endingTime = ENDING_TIME.get();

        if (startingTime != 0L && endingTime != 0L) {
            itemsPerSec = Integer.toString((int) (total / (((endingTime - startingTime) / 1000f))));
        }

        return "[" + (PROBLEMS_COUNTER.get() == 0 ? "No " : PROBLEMS_COUNTER.get()) + "] problems found " +
                "when processing [" + total + "] items. " +
                "[" + itemsPerSec + "] items per sec. " +
                "processing time was [" + getRunningTime() + "] secs\n" +
                problemsBuffer;
    }

    public String getProblemsReport() {
        return problemsBuffer.toString();
    }

    public String getRunningTime() {
        final long startingTime = STARTING_TIME.get();
        final long endingTime = ENDING_TIME.get();

        if (startingTime == 0L || endingTime == 0L) {
            return "N/A";
        }
        return Integer.toString(((int) ((endingTime - startingTime) / 1000f)));
    }
}
