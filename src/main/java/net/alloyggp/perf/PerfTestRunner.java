package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.ggp.base.util.game.Game;

import com.google.common.io.Files;

import net.alloyggp.perf.EngineType.TestCompleted;
import net.alloyggp.perf.io.CsvFiles;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.io.ResultFiles;

public class PerfTestRunner {
    private static final EngineType ENGINE_TO_TEST = EngineType.TUPLE_PROVER;
    private static final int TEST_LENGTH_SECONDS = 30;
    private static final int SECONDS_BEFORE_CANCELLING = 240;

    public static void main(String[] args) throws Exception {
        //First, make sure output dir is set up...
        File outputCsvFile = getCsvOutputFileForEngine(ENGINE_TO_TEST);

        for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
            System.out.println("Running perf test for game key: " + gameKey);

            final PerfTestResult result = runTest(gameKey, ENGINE_TO_TEST, TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

            CsvFiles.append(result, outputCsvFile);
        }
    }

    public static PerfTestResult runTest(GameKey gameKey, EngineType engineType,
            int testLengthSeconds, int secondsBeforeCancelling) throws IOException,
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
        final PerfTestResult result = interpretResults(gameKey, completed,
                results);
        return result;
    }

    private static PerfTestResult interpretResults(GameKey gameKey,
            TestCompleted completed, Map<String, String> results) {
        final PerfTestResult result;
        if (results.containsKey(CsvKeys.MILLISECONDS_TAKEN)
                && results.containsKey(CsvKeys.NUM_STATE_CHANGES)
                && results.containsKey(CsvKeys.NUM_ROLLOUTS)) {
            result = PerfTestResult.createSuccess(gameKey, ENGINE_TO_TEST.getWithVersion(),
                    Long.parseLong(results.get(CsvKeys.MILLISECONDS_TAKEN)),
                    Long.parseLong(results.get(CsvKeys.NUM_STATE_CHANGES)),
                    Long.parseLong(results.get(CsvKeys.NUM_ROLLOUTS)));
        } else {
            //Error case
            if (results.isEmpty()) {
                if (completed == TestCompleted.NO) {
                    result = PerfTestResult.createFailure(gameKey, ENGINE_TO_TEST.getWithVersion(),
                            "Test process timed out after " + SECONDS_BEFORE_CANCELLING + " seconds");
                } else {
                    result = PerfTestResult.createFailure(gameKey, ENGINE_TO_TEST.getWithVersion(),
                            "Test process failed to output results for unknown reason");
                }
            } else {
                String errorMessage = results.get(CsvKeys.ERROR_MESSAGE);
                if (errorMessage != null) {
                    result = PerfTestResult.createFailure(gameKey, ENGINE_TO_TEST.getWithVersion(),
                            errorMessage);
                } else {
                    result = PerfTestResult.createFailure(gameKey, ENGINE_TO_TEST.getWithVersion(),
                            "Test process failed to output error message for unknown reason");
                }
            }
        }
        return result;
    }

    public static File getCsvOutputFileForEngine(EngineType engineToTest) throws IOException {
        File outputDir = getOutputDir("results");
        File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
        outputCsvFile.createNewFile();
        return outputCsvFile;
    }

    //TODO: Move elsewhere
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
