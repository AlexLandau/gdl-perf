package net.alloyggp.perf.gameanalysis;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.game.Game;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * This runs Java-based game analyses.
 */
public enum GameAnalysisTask {
    /**
     * Collects some stats on how the OptimizingPropNetFactory processes
     * the game.
     */
    OPNF_STATS(OpnfStatsAnalysis.INSTANCE, 600, 4000),
;
    private final Function<Game, Map<String, String>> analysisFunction;
    private final int timeoutSeconds;
    private final int megabytesRam;

    private GameAnalysisTask(Function<Game, Map<String, String>> analysisFunction, int timeoutSeconds,
            int megabytesRam) {
        Preconditions.checkArgument(timeoutSeconds > 0);
        Preconditions.checkArgument(megabytesRam > 0);
        this.analysisFunction = analysisFunction;
        this.timeoutSeconds = timeoutSeconds;
        this.megabytesRam = megabytesRam;
    }

    public static enum Outcome {
        SUCCESS,
        TIMEOUT,
        ERROR
    }

    public Outcome runInAnotherProcess(File gameTempFile, File resultsFile) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(getJavaCommand(),
                "-cp",
                getClasspath(),
                "-Xmx"+megabytesRam+"m",
                GameAnalysisProcess.class.getName(),
                this.toString(),
                gameTempFile.getAbsolutePath(),
                resultsFile.getAbsolutePath())
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start();

        boolean success = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        if (!success) {
            System.out.println("Process timed out");
            process.destroyForcibly();
            process.waitFor();
            return Outcome.TIMEOUT;
        }

        //To be successful, the exit value should also be 0
        if (process.exitValue() == 0) {
            return Outcome.SUCCESS;
        } else {
            return Outcome.ERROR;
        }
    }

    private static String getClasspath() {
        return System.getProperty("java.class.path");
    }

    private static String getJavaCommand() {
        String command = System.getProperty("java.home") + "/bin/java";
        if (isWindows()) {
            return command + ".exe";
        }
        return command;
    }

    private static boolean isWindows() {
        //Apache commons uses this approach
        return System.getProperty("os.name").startsWith("Windows");
    }

    public Map<String, String> runInThisProcess(Game game) {
        return analysisFunction.apply(game);
    }

    /**
     * Returns the task timeout length in seconds.
     */
    public int getTimeoutLength() {
        return timeoutSeconds;
    }

}
