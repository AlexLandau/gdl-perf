package net.alloyggp.perf.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import net.alloyggp.perf.PerfTest;
import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.io.CsvFiles;

public class PerfResultLoader {

    public static ListMultimap<EngineVersion, PerfTestResult> loadResultsMap() throws IOException {
        List<PerfTestResult> allResults = loadAllResults();
        ListMultimap<EngineVersion, PerfTestResult> map = ArrayListMultimap.create();
        for (PerfTestResult result : allResults) {
            map.put(result.getEngineVersion(), result);
        }
        return map;
    }

    public static List<PerfTestResult> loadAllResults() throws IOException {
        List<PerfTestResult> allResults = Lists.newArrayList();
        for (EngineType engine : EngineType.values()) {
            allResults.addAll(loadAllResults(engine));
        }
        return allResults;
    }

    public static List<PerfTestResult> loadAllResults(EngineVersion engineVersion) throws IOException {
        return loadAllResults(engineVersion.getType())
                .stream()
                .filter(result -> result.getEngineVersion().equals(engineVersion))
                .collect(Collectors.toList());
    }

    public static List<PerfTestResult> loadAllResults(EngineType engine) throws IOException {
        File outputCsvFile = PerfTest.getCsvOutputFileForEngine(engine);
        return CsvFiles.load(outputCsvFile, PerfTestResult.getCsvLoader());
    }

}
