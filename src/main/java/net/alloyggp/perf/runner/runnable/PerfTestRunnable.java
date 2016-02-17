package net.alloyggp.perf.runner.runnable;

import net.alloyggp.perf.runner.PerfTestReport;

public interface PerfTestRunnable {
    PerfTestReport runPerfTest(String gameRules, int secondsToRun, String version) throws Exception;
}