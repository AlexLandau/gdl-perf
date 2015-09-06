package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.alloyggp.perf.io.CsvFiles;

import com.google.common.collect.Sets;

public class MissingEntriesPerfTestRunner {
    private static final EngineType ENGINE_TO_TEST = EngineType.PROVER;
    private static final int TEST_LENGTH_SECONDS = 30;
    private static final int SECONDS_BEFORE_CANCELLING = 240;

    public static void main(String[] args) throws Exception {
        File outputCsvFile = PerfTest.getCsvOutputFileForEngine(ENGINE_TO_TEST);

        Set<GameKey> gameKeysToTest = Sets.newHashSet(GameKey.loadAllValidGameKeys());
        gameKeysToTest.removeAll(loadNonfailedGameKeys(outputCsvFile));

        for (GameKey gameKey : gameKeysToTest) {
            System.out.println("Running perf test for game key: " + gameKey);

            final PerfTestResult result = PerfTest.runTest(gameKey, ENGINE_TO_TEST,
                    TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

            CsvFiles.append(result, outputCsvFile);
        }
    }

    private static Collection<GameKey> loadNonfailedGameKeys(File outputCsvFile) throws IOException {
        Set<GameKey> nonfailed = Sets.newHashSet();
        List<PerfTestResult> resultsSoFar = CsvFiles.load(outputCsvFile, PerfTestResult.getCsvLoader());

        for (PerfTestResult result : resultsSoFar) {
            if (result.wasSuccessful()) {
                nonfailed.add(result.getGameKey());
            }
        }
        return nonfailed;
    }

}
