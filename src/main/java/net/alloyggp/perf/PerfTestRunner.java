package net.alloyggp.perf;

import java.io.File;

import net.alloyggp.perf.io.CsvFiles;

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



}
