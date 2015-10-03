package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;

public class CorrectnessTest {

    public static File getCsvOutputFileForEngine(EngineType engineToTest) throws IOException {
        File outputDir = PerfTest.getOutputDir("correctnessResults");
        File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
        return outputCsvFile;
    }
}
