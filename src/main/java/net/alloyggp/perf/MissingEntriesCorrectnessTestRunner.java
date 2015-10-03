package net.alloyggp.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlPool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.istack.internal.Nullable;

import net.alloyggp.perf.io.CsvFiles;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.runner.GameActionMessage;
import net.alloyggp.perf.runner.GameActionParser;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.TimeoutSignaler;

//TODO: Supplement with a model of "bring each game's testing up to a certain amount"
public class MissingEntriesCorrectnessTestRunner {
    private static final List<EngineType> ENGINES_TO_TEST =
//            ImmutableList.of(EngineType.REKKURA_BACKWARD_PROVER);
            ImmutableList.copyOf(EngineType.values());
    //To make things simpler, restrict validation to the Java engine types
    //TODO: Find a faster known-good reference engine for testing
    private static final JavaEngineType VALIDATION_ENGINE = JavaEngineType.PROVER;
//    private static final int MIN_NUM_STATE_CHANGES_TO_TEST = 1000;
    private static final int INITIAL_NUM_STATE_CHANGES_TO_TEST = 5;
    private static final int MIN_SECONDS_PER_TEST = 30; //Should be under 1/3rd of max
    private static final int MAX_SECONDS_PER_TEST = 240;

    public static void main(String[] args) throws Exception {
        GdlPool.caseSensitive = false;
        for (EngineType engineToTest : ENGINES_TO_TEST) {
            System.out.println("Testing engine " + engineToTest);
            File outputCsvFile = CorrectnessTest.getCsvOutputFileForEngine(engineToTest);

            Set<GameKey> alreadyTestedGames = loadAlreadyTestedGames(outputCsvFile);
            for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
                if (alreadyTestedGames.contains(gameKey)) {
                    continue;
                }
                System.out.println("Testing game " + gameKey);
                int numStateChangesToTest = INITIAL_NUM_STATE_CHANGES_TO_TEST;
                long overallStartTime = System.currentTimeMillis();
                long iterationStartTime = System.currentTimeMillis();
                try {
                    while (true) {
                        iterationStartTime = System.currentTimeMillis();
                        CorrectnessTestResult result = runTest(numStateChangesToTest, engineToTest, VALIDATION_ENGINE, gameKey);
                        if (result != null) {
                            CsvFiles.append(result, outputCsvFile);
                        }
                        long overallTimeTaken = System.currentTimeMillis() - overallStartTime;
                        if (result == null
                                || result.getError().isPresent()
                                || overallTimeTaken >= MIN_SECONDS_PER_TEST * 1000) {
                            break;
                        }
                        //Keep going...
                        numStateChangesToTest *= 2;
                    }
                } catch (Exception e) {
                    ObservedError error = ObservedError.create(e.getMessage(), 0);
                    long iterationTimeTaken = System.currentTimeMillis() - iterationStartTime;
                    CorrectnessTestResult result = CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(),
                            VALIDATION_ENGINE, VALIDATION_ENGINE.getVersion(), iterationTimeTaken, 0, Optional.of(error));
                    CsvFiles.append(result, outputCsvFile);
                }
                GdlPool.drainPool();
                System.gc();
            }
        }
    }

    private static Set<GameKey> loadAlreadyTestedGames(File outputCsvFile) throws IOException {
        List<CorrectnessTestResult> results = CsvFiles.load(outputCsvFile, CorrectnessTestResult.getCsvLoader());
        return results.stream()
                .map(CorrectnessTestResult::getGameKey)
                .collect(Immutables.collectSet());
    }


    //TODO: Wrap all this in a big try block
    private static @Nullable CorrectnessTestResult runTest(int numStateChangesToTest,
            EngineType engineToTest,
            JavaEngineType validationEngine, GameKey gameKey) throws Exception {
        Game game = gameKey.loadGame();
        File gameFile = File.createTempFile("game", ".kif");
        GameFiles.write(game, gameFile);

        //TODO: Add right set of commands
        List<String> commands = Lists.newArrayList(engineToTest.getCommandsForCorrectnessTest());
        commands.add(gameFile.getAbsolutePath());
        commands.add(Integer.toString(numStateChangesToTest));

        ProcessBuilder pb = new ProcessBuilder(commands);
        Process process = pb.start();
        AtomicBoolean timedOut = new AtomicBoolean(false);

        TimeoutSignaler timeoutSignaler = new TimeoutSignaler();
        timeoutSignaler.onTimeoutDestroyForcibly(process);

        long startTime = System.currentTimeMillis();
        Callable<Optional<ObservedError>> validationCallable = () -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                //TODO: This creates another thread we need to ___...
                BlockingQueue<GameActionMessage> queue = GameActionParser.convert(in, timeoutSignaler);
                return validationEngine.validateCorrectnessTestOutput(game, queue);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                process.destroyForcibly();
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Optional<ObservedError>> errorFuture = executor.submit(validationCallable);
        timeoutSignaler.onTimeoutShutdownNow(executor);

        CountDownLatch finishedSignal = new CountDownLatch(1);
        startTimeoutThread(timedOut, finishedSignal, timeoutSignaler, MAX_SECONDS_PER_TEST);
        process.waitFor();
        Optional<ObservedError> error = errorFuture.get();
        finishedSignal.countDown(); //cleans up stuff
        long timeTaken = System.currentTimeMillis() - startTime;
        if (timedOut.get()) {
            error = Optional.of(ObservedError.create("Timed out after " + MAX_SECONDS_PER_TEST + " seconds", 0));
            return CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(), validationEngine,
                    validationEngine.getVersion(), timeTaken, numStateChangesToTest, error);
        } else if (error == null) {
            System.out.println("No results; validation failed");
            return null;
        } else {
            if (error.isPresent()) {
                numStateChangesToTest = error.get().getNumStateChangesBeforeFinding();
            }
            return CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(), validationEngine,
                    validationEngine.getVersion(), timeTaken, numStateChangesToTest, error);
        }
    }

    private static void startTimeoutThread(AtomicBoolean timedOut,
            CountDownLatch finishedSignal, TimeoutSignaler timeoutSignaler, int maxSecondsPerTest) {
        Runnable runnable = () -> {
            try {
                boolean finishedNormally = finishedSignal.await(maxSecondsPerTest, TimeUnit.SECONDS);
                if (!finishedNormally) {
                    System.out.println("Timing out...");
                    timedOut.set(true);
                }
                timeoutSignaler.signalTimeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        new Thread(runnable, "Correctness test timeout thread").start();
    }
}
