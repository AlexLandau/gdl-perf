package net.alloyggp.perf;

import java.io.File;
import java.util.List;
import java.util.Set;

import net.alloyggp.perf.io.CsvFiles;

import com.google.common.collect.Lists;

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
