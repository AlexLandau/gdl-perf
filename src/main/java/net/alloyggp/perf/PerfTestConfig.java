package net.alloyggp.perf;

import java.io.File;

import com.google.common.base.Preconditions;

public class PerfTestConfig {
    private final File gameFile;
    private final File outputFile;
    private final int testLengthSeconds;
    private final int secondsBeforeCancelling;

    public PerfTestConfig(File gameFile, File outputFile, int testLengthSeconds, int secondsBeforeCancelling) {
        Preconditions.checkArgument(testLengthSeconds > 0);
        Preconditions.checkArgument(secondsBeforeCancelling > 0);
        Preconditions.checkArgument(testLengthSeconds < secondsBeforeCancelling,
                "The target test length should be less than the time before cancelling the test");
        this.gameFile = gameFile;
        this.outputFile = outputFile;
        this.testLengthSeconds = testLengthSeconds;
        this.secondsBeforeCancelling = secondsBeforeCancelling;
    }

    public File getGameFile() {
        return gameFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public int getNumSeconds() {
        return testLengthSeconds;
    }

    public long getSecondsBeforeCancelling() {
        return secondsBeforeCancelling;
    }

    @Override
    public String toString() {
        return "PerfTestConfig [gameFile=" + gameFile + ", outputFile="
                + outputFile + ", testLengthSeconds=" + testLengthSeconds
                + ", secondsBeforeCancelling=" + secondsBeforeCancelling + "]";
    }
}
