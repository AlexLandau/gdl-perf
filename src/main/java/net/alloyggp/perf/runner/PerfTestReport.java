package net.alloyggp.perf.runner;

import java.util.Map;

import net.alloyggp.perf.CsvKeys;

import com.google.common.collect.ImmutableMap;

public class PerfTestReport {
    private final long millisecondsTaken;
    private final long numStateChanges;
    private final long numRollouts;

    public PerfTestReport(long millisecondsTaken, long numStateChanges,
            long numRollouts) {
        this.millisecondsTaken = millisecondsTaken;
        this.numStateChanges = numStateChanges;
        this.numRollouts = numRollouts;
    }

    public long getMillisecondsTaken() {
        return millisecondsTaken;
    }

    public long getNumStateChanges() {
        return numStateChanges;
    }

    public long getNumRollouts() {
        return numRollouts;
    }

    public Map<String, String> toKeyValuePairs() {
        return ImmutableMap.of(
                CsvKeys.MILLISECONDS_TAKEN, Long.toString(millisecondsTaken),
                CsvKeys.NUM_STATE_CHANGES, Long.toString(numStateChanges),
                CsvKeys.NUM_ROLLOUTS, Long.toString(numRollouts));
    }
}
