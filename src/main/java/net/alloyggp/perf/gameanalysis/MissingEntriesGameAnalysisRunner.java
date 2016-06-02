package net.alloyggp.perf.gameanalysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.gameanalysis.GameAnalysisTask.Outcome;
import net.alloyggp.perf.io.CsvFiles;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.io.ResultFiles;

public class MissingEntriesGameAnalysisRunner {
    private static final List<GameAnalysisTask> ANALYSES_TO_RUN =
          ImmutableList.copyOf(GameAnalysisTask.values());

    public static void main(String[] args) throws Exception {
        for (GameAnalysisTask task : ANALYSES_TO_RUN) {
            System.out.println("Running game analysis task " + task);

            File outputCsvFile = CsvFiles.getCsvOutputFileForGameAnalysisTask(task);

            Set<GameKey> gameKeysToTest = Sets.newHashSet(GameKey.loadAllValidGameKeys());
            gameKeysToTest.removeAll(loadAllGameKeys(outputCsvFile));

            for (GameKey gameKey : gameKeysToTest) {
                System.out.println("Analyzing " + gameKey);
                runAnalysis(gameKey, task, outputCsvFile);
            }
        }
    }

    private static void runAnalysis(GameKey gameKey, GameAnalysisTask task, File outputCsvFile) throws IOException, InterruptedException {
        File gameTempFile = File.createTempFile("game", ".kif");
        GameFiles.write(gameKey.loadGame(), gameTempFile);
        File resultsFile = File.createTempFile("results", ".txt");

        Outcome outcome = task.runInAnotherProcess(gameTempFile, resultsFile);

        final Map<String, String> results;
        if (outcome == Outcome.SUCCESS) {
            results = ResultFiles.read(resultsFile);
        } else if (outcome == Outcome.TIMEOUT) {
            //Task timed out
            results = ImmutableMap.of(task.toString(), "Timeout: " + task.getTimeoutLength());
        } else {
            results = ImmutableMap.of(task.toString(), "Error");
        }
        for (Entry<String, String> entry : results.entrySet()) {
            CsvFiles.append(GameAnalysisResult.create(gameKey, entry.getKey(), entry.getValue()), outputCsvFile);
        }
    }

    private static Collection<GameKey> loadAllGameKeys(File outputCsvFile) throws IOException {
        Set<GameKey> gameKeys = Sets.newHashSet();
        List<GameAnalysisResult> resultsSoFar = CsvFiles.load(outputCsvFile, GameAnalysisResult.getCsvLoader());

        for (GameAnalysisResult result : resultsSoFar) {
            gameKeys.add(result.getGameKey());
        }
        return gameKeys;
    }
}
