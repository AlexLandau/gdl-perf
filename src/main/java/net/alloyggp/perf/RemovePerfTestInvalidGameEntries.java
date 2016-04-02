package net.alloyggp.perf;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.game.InvalidGames;
import net.alloyggp.perf.io.CsvFiles;

/**
 * If a game is found to be invalid after perf results for the game have
 * already been collected, this can be run to remove the results for that
 * invalid game.
 */
public class RemovePerfTestInvalidGameEntries {

    public static void main(String[] args) throws Exception {
        Set<GameKey> invalidGames = InvalidGames.loadInvalidGames().keySet();
        for (EngineType engine : EngineType.values()) {
            File csvFile = PerfTest.getCsvOutputFileForEngine(engine);
            List<PerfTestResult> results = CsvFiles.load(csvFile, PerfTestResult.getCsvLoader());
            List<PerfTestResult> newResults = Lists.newArrayList();
            for (PerfTestResult result : results) {
                if (isValid(result, invalidGames)) {
                    newResults.add(result);
                }
            }
            CsvFiles.rewrite(csvFile, newResults);
        }
    }

    private static boolean isValid(PerfTestResult result, Set<GameKey> invalidGames) {
        if (invalidGames.contains(result.getGameKey())) {
            return false;
        }
        return true;
    }

}
