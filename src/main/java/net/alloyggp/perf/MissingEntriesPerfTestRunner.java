package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import net.alloyggp.perf.io.CsvFiles;

public class MissingEntriesPerfTestRunner {
    private static final boolean RETRY_FAILURES = false;
    private static final List<EngineType> ENGINES_TO_TEST =
//            ImmutableList.of(EngineType.REKKURA_BACKWARD_PROVER);
            ImmutableList.copyOf(EngineType.values());
    private static final int TEST_LENGTH_SECONDS = 30;
    private static final int SECONDS_BEFORE_CANCELLING = 240;

    public static void main(String[] args) throws Exception {
        for (EngineType engineToTest : ENGINES_TO_TEST) {
            System.out.println("Testing engine " + engineToTest);

            File outputCsvFile = PerfTest.getCsvOutputFileForEngine(engineToTest);

            Set<GameKey> gameKeysToTest = Sets.newHashSet(GameKey.loadAllValidGameKeys());
            if (RETRY_FAILURES) {
                gameKeysToTest.removeAll(loadNonfailedGameKeys(outputCsvFile, engineToTest.getWithVersion()));
            } else {
                gameKeysToTest.removeAll(loadAllGameKeys(outputCsvFile, engineToTest.getWithVersion()));
            }

            if (gameKeysToTest.isEmpty()) {
                // Skip the engine test
                continue;
            }
            System.out.println("Checking if engine can run on this computer...");
            CompatibilityResult compatible = engineToTest.runCompatibilityTest();
            if (compatible.isCompatible()) {
                System.out.println("Compatibility test successful");
            } else {
                System.out.println("Compatibility test failed, skipping engine");
                continue;
            }

            for (GameKey gameKey : gameKeysToTest) {
                System.out.println("Running perf test for game key: " + gameKey);

                if (gameKey.isValid()) {
                    final PerfTestResult result = PerfTest.runTest(gameKey, engineToTest,
                            compatible.getVersion(), TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

                    CsvFiles.append(result, outputCsvFile);
                }
            }
        }
    }

    private static Collection<GameKey> loadAllGameKeys(File outputCsvFile, EngineVersion engineVersion) throws IOException {
        Set<GameKey> gameKeys = Sets.newHashSet();
        List<PerfTestResult> resultsSoFar = CsvFiles.load(outputCsvFile, PerfTestResult.getCsvLoader());

        for (PerfTestResult result : resultsSoFar) {
            if (result.getEngineVersion().equals(engineVersion)) {
                gameKeys.add(result.getGameKey());
            }
        }
        return gameKeys;
    }

    private static Collection<GameKey> loadNonfailedGameKeys(File outputCsvFile, EngineVersion engineVersion) throws IOException {
        Set<GameKey> nonfailed = Sets.newHashSet();
        List<PerfTestResult> resultsSoFar = CsvFiles.load(outputCsvFile, PerfTestResult.getCsvLoader());

        for (PerfTestResult result : resultsSoFar) {
            if (result.wasSuccessful() && result.getEngineVersion().equals(engineVersion)) {
                nonfailed.add(result.getGameKey());
            }
        }
        return nonfailed;
    }

}
