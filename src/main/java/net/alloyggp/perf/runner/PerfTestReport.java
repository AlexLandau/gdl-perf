package net.alloyggp.perf.runner;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.alloyggp.perf.CsvKeys;

public class PerfTestReport {
    private final String version;
    private final long millisecondsTaken;
    private final long numStateChanges;
    private final long numRollouts;

    public PerfTestReport(String version, long millisecondsTaken, long numStateChanges, long numRollouts) {
        this.version = version;
        this.millisecondsTaken = millisecondsTaken;
        this.numStateChanges = numStateChanges;
        this.numRollouts = numRollouts;
    }

    public String getVersion() {
        return version;
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
                CsvKeys.VERSION, version,
                CsvKeys.MILLISECONDS_TAKEN, Long.toString(millisecondsTaken),
                CsvKeys.NUM_STATE_CHANGES, Long.toString(numStateChanges),
                CsvKeys.NUM_ROLLOUTS, Long.toString(numRollouts));
    }
}
