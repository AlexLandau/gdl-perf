package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import net.alloyggp.perf.io.CsvFiles;

import com.google.common.io.Files;

public class PerfTestRunner {
    private static final EngineType ENGINE_TO_TEST = EngineType.PROVER;
    private static final int TEST_LENGTH_SECONDS = 30;
    private static final int SECONDS_BEFORE_CANCELLING = 240;

    public static void main(String[] args) throws Exception {
        //First, make sure output dir is set up...
        File outputCsvFile = PerfTest.getCsvOutputFileForEngine(ENGINE_TO_TEST);

        for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
            System.out.println("Running perf test for game key: " + gameKey);

            final PerfTestResult result = PerfTest.runTest(gameKey, ENGINE_TO_TEST, TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

            CsvFiles.append(result, outputCsvFile);
        }
    }

    static String loadComputerNameForOutputDir() {
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

    static void ensureIsDirectory(File dir) {
        dir.mkdirs();
        if (!dir.isDirectory()) {
            throw new RuntimeException("Couldn't ensure existence of directory at path: " + dir.getAbsolutePath());
        }
    }

}
