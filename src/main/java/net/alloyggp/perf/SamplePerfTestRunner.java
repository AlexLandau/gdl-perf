package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.game.RepoId;
import net.alloyggp.perf.io.CsvFiles;

//TODO: Merge better with MissingEntriesPerfTestRunner
public class SamplePerfTestRunner {
    private final ImmutableSet<EngineType> enginesToTest =
            ImmutableSet.of(EngineType.GGP_BASE_PROVER,
                    EngineType.PALAMEDES_JOCULAR,
                    EngineType.REKKURA_BACKWARD_PROVER,
                    EngineType.PALAMEDES_JAVA_PROVER_USEOPT_TRUE,
                    EngineType.SANCHO_DEAD_RECKONING_PROPNET);
    private final ImmutableSet<GameKey> gamesToTest =
            GameKey.createSet(RepoId.BASE,
                    "ticTacToe",
                    "connectFour",
                    "hex",
                    "speedChess",
                    "reversi");
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

            CompatibilityResult compatible = engineToTest.runCompatibilityTest();
            Preconditions.checkState(compatible.isCompatible(), "Somehow the simple compatibility test failed");

            for (GameKey gameKey : gamesToTest) {
                System.out.println("Running perf test for game key: " + gameKey);

                if (gameKey.isValid()) {
                    final PerfTestResult result = PerfTest.runTest(gameKey, engineToTest,
                            compatible.getVersion(), TEST_LENGTH_SECONDS, SECONDS_BEFORE_CANCELLING);

                    CsvFiles.append(result, outputCsvFile);
                }
            }
        }
    }
}
