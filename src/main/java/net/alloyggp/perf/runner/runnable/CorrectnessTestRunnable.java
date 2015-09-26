package net.alloyggp.perf.runner.runnable;

import net.alloyggp.perf.runner.GameActionRecorder;

public interface CorrectnessTestRunnable {
    void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder) throws Exception;
}