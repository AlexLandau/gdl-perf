package net.alloyggp.perf.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import net.alloyggp.perf.CorrectnessTest;
import net.alloyggp.perf.CorrectnessTestResult;
import net.alloyggp.perf.EngineType;
import net.alloyggp.perf.EngineVersion;
import net.alloyggp.perf.io.CsvFiles;

public class CorrectnessResultLoader {

    public static ListMultimap<EngineVersion, CorrectnessTestResult> loadResultsMap() throws IOException {
        List<CorrectnessTestResult> allResults = loadAllResults();
        ListMultimap<EngineVersion, CorrectnessTestResult> map = ArrayListMultimap.create();
        for (CorrectnessTestResult result : allResults) {
            map.put(result.getTestedEngine(), result);
        }
        return map;
    }

    public static List<CorrectnessTestResult> loadAllResults() throws IOException {
        List<CorrectnessTestResult> allResults = Lists.newArrayList();
        for (EngineType engine : EngineType.values()) {
            allResults.addAll(loadAllResults(engine));
        }
        return allResults;
    }

    public static List<CorrectnessTestResult> loadAllResults(EngineType engine) throws IOException {
        File outputCsvFile = CorrectnessTest.getCsvOutputFileForEngine(engine);
        return CsvFiles.load(outputCsvFile, CorrectnessTestResult.getCsvLoader());
    }

}
