package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.io.CsvFiles;

public class RemoveEngineResults {

    public static void main(String[] args) throws IOException {

        String providedEngineVersion = System.getenv("ENGINE_VERSION"); //Something they supply

        if (providedEngineVersion == null) {
            System.out.println("Provide an ENGINE_VERSION environment variable when running this!"
                    + " For example: 'ENGINE_VERSION=\"MY_ENGINE:2.1.0\" ./gradlew removeEngineResults'");
        }

        String[] split = providedEngineVersion.split(":");
        if (split.length != 2) {
            throw new RuntimeException("Expected format ENGINE:VERSION");
        }
        String engineType = split[0];
        String version = split[1];

        EngineVersion engineVersion = EngineVersion.parse(engineType, version);

        File csvFileToEdit = PerfTest.getCsvOutputFileForEngine(engineVersion.getType());

        List<PerfTestResult> allResults = CsvFiles.load(csvFileToEdit, PerfTestResult.getCsvLoader());

        allResults.removeIf(result -> result.getEngineVersion().equals(engineVersion));

        CsvFiles.rewrite(csvFileToEdit, allResults);
    }

}
