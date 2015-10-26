package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;

import com.google.common.collect.ImmutableSet;

import net.alloyggp.perf.io.CsvFiles;

//TODO: Merge better with MissingEntriesPerfTestRunner
public class SamplePerfTestRunner {
    private final ImmutableSet<EngineType> enginesToTest =
            ImmutableSet.of(EngineType.PROVER,
                    EngineType.PALAMEDES_JOCULAR,
                    EngineType.REKKURA_BACKWARD_PROVER,
                    EngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE);
    private final ImmutableSet<GameKey> gamesToTest =
            GameKey.createSet("BASE/ticTacToe",
                    "BASE/connectFour",
                    "BASE/hex",
                    "BASE/speedChess",
                    "BASE/reversi");
    private static final int TEST_LENGTH_SECONDS = 15;
    private static final int SECONDS_BEFORE_CANCELLING = 240;

    private SamplePerfTestRunner() {
        //Not instantiable
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new SamplePerfTestRunner().run();
    }

    private void run() throws IOException, InterruptedException {
        for (EngineType engineToTest : enginesToTest) {
            System.out.println("Testing engine " + engineToTest);
            File outputCsvFile = PerfTest.getCsvOutputFileForEngine(engineToTest);

            for (GameKey gameKey : gamesToTest) {
                System.out.println("Running perf test for game key: " + gameKey);

                final PerfTestResult result = PerfTest.runTest(gameKey, engineToTest,
                        TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

                CsvFiles.append(result, outputCsvFile);
            }
        }
    }
}
