package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.ggp.base.util.game.Game;

import com.google.common.io.Files;

import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.engine.EngineType.TestCompleted;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.io.ResultFiles;

public class PerfTest {

    public static PerfTestResult runTest(GameKey gameKey, EngineType engineType,
            String defaultVersion, int testLengthSeconds, int secondsBeforeCancelling) throws IOException,
            InterruptedException {
        File gameFile = File.createTempFile("game", ".kif");
        Game game = gameKey.loadGame();
        GameFiles.write(game, gameFile);

        File outputFile = File.createTempFile("results", ".txt");

        PerfTestConfig perfTestConfig = new PerfTestConfig(gameFile, outputFile, testLengthSeconds,
                secondsBeforeCancelling);
        TestCompleted completed = engineType.runPerfTest(perfTestConfig);

        Map<String, String> results = ResultFiles.read(outputFile);
        //TODO: Store results...
        final PerfTestResult result = interpretResults(engineType, defaultVersion, gameKey, completed,
                results, secondsBeforeCancelling);
        return result;
    }


    private static PerfTestResult interpretResults(EngineType engineToTest, String defaultVersion, GameKey gameKey,
            TestCompleted completed, Map<String, String> results, int secondsBeforeCancelling) {

        final PerfTestResult result;
        if (results.containsKey(CsvKeys.MILLISECONDS_TAKEN)
                && results.containsKey(CsvKeys.NUM_STATE_CHANGES)
                && results.containsKey(CsvKeys.NUM_ROLLOUTS)) {
            if (!results.containsKey(CsvKeys.VERSION)) {
                throw new RuntimeException("The perf test implementation needs to provide a 'version' in its result file");
            }
            result = PerfTestResult.createSuccess(gameKey, engineToTest,
                    results.get(CsvKeys.VERSION),
                    Long.parseLong(results.get(CsvKeys.MILLISECONDS_TAKEN)),
                    Long.parseLong(results.get(CsvKeys.NUM_STATE_CHANGES)),
                    Long.parseLong(results.get(CsvKeys.NUM_ROLLOUTS)));
        } else {
            //Error case
            if (results.isEmpty()) {
                EngineVersion withVersion = EngineVersion.create(engineToTest, defaultVersion);
                if (completed == TestCompleted.NO) {
                    result = PerfTestResult.createFailure(gameKey, withVersion,
                            "Test process timed out after " + secondsBeforeCancelling + " seconds");
                } else {
                    result = PerfTestResult.createFailure(gameKey, withVersion,
                            "Test process failed to output results for unknown reason");
                }
            } else {
                if (!results.containsKey(CsvKeys.VERSION)) {
                    throw new RuntimeException("The perf test implementation needs to provide a 'version' in its result file");
                }
                EngineVersion withVersion = EngineVersion.create(engineToTest, results.get(CsvKeys.VERSION));
                String errorMessage = results.get(CsvKeys.ERROR_MESSAGE);
                if (errorMessage != null) {
                    result = PerfTestResult.createFailure(gameKey, withVersion,
                            errorMessage);
                } else {
                    result = PerfTestResult.createFailure(gameKey, withVersion,
                            "Test process failed to output error message for unknown reason");
                }
            }
        }
        return result;
    }

    public static File getCsvOutputFileForEngine(EngineType engineToTest) {
        File outputDir = PerfTest.getOutputDir("results");
        File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
        return outputCsvFile;
    }

    public static File getOutputDir(String resultsDirName) {
        File resultsDir = new File(resultsDirName);
        ensureIsDirectory(resultsDir);

        //Figure out the right output directory for this computer
        String computerName = loadComputerNameForOutputDir();
        File outputDir = new File(resultsDir, computerName);
        ensureIsDirectory(outputDir);

        return outputDir;
    }

    private static String loadComputerNameForOutputDir() {
        File file = new File("computerName.txt");
        try {
            return Files.readFirstLine(file, Charset.defaultCharset()).trim();
        } catch (IOException e) {
            throw new RuntimeException("To run tests, you must have a file named computerName.txt "
                    + "containing just your computer ID in the gdl-perf directory. The folder containing "
                    + "your test results will use this name. The preferred "
                    + "format (if applicable) is your GitHub user name followed by an integer to "
                    + "distinguish between your computers, e.g. \"AlexLandau1\".", e);
        }
    }

    private static void ensureIsDirectory(File dir) {
        dir.mkdirs();
        if (!dir.isDirectory()) {
            throw new RuntimeException("Couldn't ensure existence of directory at path: " + dir.getAbsolutePath());
        }
    }
}
