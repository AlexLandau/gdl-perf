package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.game.GameKey;
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
            File outputCsvFile = PerfTest.getCsvOutputFileForEngine(engineToTest);
            Set<GameKey> gameKeysToTest = Sets.newHashSet(GameKey.loadAllValidGameKeys());
            System.out.println("Testing engine " + engineToTest);
            if (isTestingUnnecessary(engineToTest, outputCsvFile, gameKeysToTest)) {
                System.out.println("Testing already done, skipping engine");
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


            if (RETRY_FAILURES) {
                gameKeysToTest.removeAll(loadNonfailedGameKeys(outputCsvFile, engineToTest.getWithVersion(compatible.getVersion())));
            } else {
                gameKeysToTest.removeAll(loadAllGameKeys(outputCsvFile, engineToTest.getWithVersion(compatible.getVersion())));
            }

            if (gameKeysToTest.isEmpty()) {
                // Skip the engine test
                continue;
            }

            List<GameKey> gameKeysList = ImmutableList.copyOf(gameKeysToTest);
            for (int i = 0; i < gameKeysList.size(); i++) {
                GameKey gameKey = gameKeysList.get(i);
                System.out.println("Running perf test " + (i+1) + "/" + gameKeysList.size() + " for " + engineToTest + ":" + compatible.getVersion() + ": " + gameKey);

                if (gameKey.isValid()) {
                    final PerfTestResult result = PerfTest.runTest(gameKey, engineToTest,
                            compatible.getVersion(), TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

                    CsvFiles.append(result, outputCsvFile);
                }
            }
        }
    }

    /**
     * If the engine is Java-based, then we already know its version and can check
     * whether it has any more work to do before running a compatibility test.
     */
    private static boolean isTestingUnnecessary(EngineType engineToTest, File outputCsvFile, Set<GameKey> gameKeysToTest) throws IOException {
        if (engineToTest.getJavaEngineType().isPresent()) {
            String version = engineToTest.getJavaEngineType().get().getVersion();
            EngineVersion engineVersion = engineToTest.getWithVersion(version);
            Set<GameKey> alreadyPlayedGames = ImmutableSet.copyOf(loadAllGameKeys(outputCsvFile, engineVersion));
            return alreadyPlayedGames.containsAll(gameKeysToTest);
        }
        return false;
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
