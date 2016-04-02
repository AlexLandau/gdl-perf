package net.alloyggp.perf.correctness;

import java.io.File;
import java.io.IOException;

import net.alloyggp.perf.PerfTest;
import net.alloyggp.perf.engine.EngineType;

public class CorrectnessTest {

    public static File getCsvOutputFileForEngine(EngineType engineToTest) throws IOException {
        File outputDir = PerfTest.getOutputDir("correctnessResults");
        File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
        return outputCsvFile;
    }
}
