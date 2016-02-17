package net.alloyggp.perf.runner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.alloyggp.perf.CsvKeys;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.io.ResultFiles;

/**
 * This is the process that runs the actual perf testing for Java-based engines.
 */
public class PerfTestProcess {

    public static void main(String[] args) throws IOException {
        JavaEngineType engine = JavaEngineType.valueOf(args[0]);
        File gameFile = new File(args[1]);
        File outputFile = new File(args[2]);
        int secondsToRun = Integer.parseInt(args[3]);

        String gameRules = GameFiles.read(gameFile);

        try {
            PerfTestReport result = engine.runPerfTest(gameRules, secondsToRun);
            ResultFiles.write(result.toKeyValuePairs(), outputFile);
        } catch (Throwable e) {
            ResultFiles.write(toErrorResult(engine, e), outputFile);
        }
    }

    private static Map<String, String> toErrorResult(JavaEngineType engine, Throwable e) {
        if (e.getMessage() == null) {
            return ImmutableMap.of(CsvKeys.VERSION, engine.getVersion(),
                    CsvKeys.ERROR_MESSAGE, "Exception of type " + e.getClass().getSimpleName());
        } else {
            return ImmutableMap.of(CsvKeys.VERSION, engine.getVersion(),
                    CsvKeys.ERROR_MESSAGE, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


}
