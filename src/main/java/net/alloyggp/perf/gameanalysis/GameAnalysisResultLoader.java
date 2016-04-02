package net.alloyggp.perf.gameanalysis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import net.alloyggp.perf.io.CsvFiles;

public class GameAnalysisResultLoader {
    private GameAnalysisResultLoader() {
        //Not instantiable
    }

    public static List<GameAnalysisResult> loadAllResults() throws IOException {
        List<GameAnalysisResult> allResults = Lists.newArrayList();
        for (GameAnalysisTask task : GameAnalysisTask.values()) {
            allResults.addAll(loadAllResults(task));
        }
        return allResults;
    }

    private static List<GameAnalysisResult> loadAllResults(GameAnalysisTask task) throws IOException {
        File file = CsvFiles.getCsvOutputFileForGameAnalysisTask(task);
        return CsvFiles.load(file, GameAnalysisResult.getCsvLoader());
    }
}
