package net.alloyggp.perf.correctness;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;
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

import javax.annotation.Nullable;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.python.google.common.collect.Sets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.io.Files;

import net.alloyggp.perf.CompatibilityResult;
import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.io.CsvFiles;
import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.runner.GameActionMessage;
import net.alloyggp.perf.runner.GameActionParser;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.TimeoutSignaler;

//TODO: Supplement with a model of "bring each game's testing up to a certain amount"
public class MissingEntriesCorrectnessTestRunner {
    private static final List<EngineType> ENGINES_TO_TEST =
            ImmutableList.of(
                    EngineType.ALLOY_FORWARD_PROP_NET,
                    EngineType.ALLOY_FORWARD_PROP_NET2,
                    EngineType.ALLOY_FORWARD_PROP_NET3,
                    EngineType.ALLOY_FORWARD_PROP_NET4
                    );
//            ImmutableList.copyOf(EngineType.values());
    //To make things simpler, restrict validation to the Java engine types
    //TODO: Find a faster known-good reference engine for testing
    private static final JavaEngineType VALIDATION_ENGINE = JavaEngineType.GGP_BASE_PROVER;
//    private static final int MIN_NUM_STATE_CHANGES_TO_TEST = 1000;
    private static final int INITIAL_NUM_STATE_CHANGES_TO_TEST = 5;
    private static final int MIN_SECONDS_PER_GAME = 30; //Should be under 1/3rd of max
    private static final int MAX_SECONDS_PER_TEST = 240;

    public static void main(String[] args) throws Exception {
        GdlPool.caseSensitive = false;
        while (true) {
            boolean done = runOneRound();
            if (done) {
                break;
            }
        }
    }

    /**
     * @return true if there's nothing left to run, false otherwise
     */
    private static boolean runOneRound() throws IOException, InterruptedException {
        boolean ranAnyTest = false;
        for (EngineType engineToTest : ENGINES_TO_TEST) {
            if (engineToTest.getJavaEngineType().isPresent()
                    && engineToTest.getJavaEngineType().get() == VALIDATION_ENGINE) {
                System.out.println("Skipping " + engineToTest + ", as it is the validation engine.");
                continue;
            }
            System.out.println("Testing engine " + engineToTest);
            if (engineToTest.getCommandsForCorrectnessTest().isEmpty()) {
                System.out.println(engineToTest + " does not support correctness tests, skipping.");
                continue;
            }
            File outputCsvFile = CorrectnessTest.getCsvOutputFileForEngine(engineToTest);

            System.out.println("Checking if engine can run on this computer...");
            CompatibilityResult compatible = engineToTest.runCompatibilityTest();
            if (compatible.isCompatible()) {
                System.out.println("Compatibility test successful");
            } else {
                System.out.println("Compatibility test failed, skipping engine");
                continue;
            }

            Map<GameKey, AggregateResult> earlierResults = loadAlreadyTestedGames(outputCsvFile);
            Set<GameKey> allValidGameKeys = GameKey.loadAllValidGameKeys();
            long minMillisSpentOnAnyGame = getMinMillisSpentOnAnyGame(earlierResults, allValidGameKeys);

            long maxMillisToSpend = MIN_SECONDS_PER_GAME * 1000 + minMillisSpentOnAnyGame;

            for (GameKey gameKey : allValidGameKeys) {
                if (earlierResults.containsKey(gameKey)
                        && earlierResults.get(gameKey).isFailure()) {
                    continue;
                }
                if (earlierResults.containsKey(gameKey)
                        && earlierResults.get(gameKey).getMillisSpentSoFar() > maxMillisToSpend) {
                    continue;
                }
                if (!gameKey.isValid()) {
                    continue;
                }
                System.out.println("Testing game " + gameKey);
                ranAnyTest = true;

                int numStateChangesToTest = INITIAL_NUM_STATE_CHANGES_TO_TEST;
                if (earlierResults.containsKey(gameKey)) {
                    numStateChangesToTest = earlierResults.get(gameKey).getMostStateChangesSoFar();
                }

                long overallStartTime = System.currentTimeMillis();
                long iterationStartTime = System.currentTimeMillis();
                try {
                    while (true) {
                        iterationStartTime = System.currentTimeMillis();
                        CorrectnessTestResult result = runTest(numStateChangesToTest, engineToTest, compatible.getVersion(), VALIDATION_ENGINE, gameKey);
                        if (result != null) {
                            CsvFiles.append(result, outputCsvFile);
                        }
                        long overallTimeTaken = System.currentTimeMillis() - overallStartTime;
                        if (result == null
                                || result.getError().isPresent()
                                || overallTimeTaken >= MIN_SECONDS_PER_GAME * 1000) {
                            break;
                        }
                        //Keep going...
                        numStateChangesToTest *= 2;
                    }
                } catch (Exception e) {
                    ObservedError error = ObservedError.create(e.getMessage(), 0);
                    long iterationTimeTaken = System.currentTimeMillis() - iterationStartTime;
                    CorrectnessTestResult result = CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(compatible.getVersion()),
                            VALIDATION_ENGINE, VALIDATION_ENGINE.getVersion(), iterationTimeTaken, 0, Optional.of(error));
                    CsvFiles.append(result, outputCsvFile);
                }
                GdlPool.drainPool();
                System.gc();
            }
        }
        return !ranAnyTest;
    }

    private static long getMinMillisSpentOnAnyGame(Map<GameKey, AggregateResult> earlierResults,
            Set<GameKey> allValidGameKeys) {
        if (!Sets.difference(allValidGameKeys, earlierResults.keySet()).isEmpty()) {
            //At least one valid game is untested
            return 0L;
        }
        long minMillisSpent = Long.MAX_VALUE;
        for (GameKey validGame : allValidGameKeys) {
            long millisSpent = earlierResults.get(validGame).getMillisSpentSoFar();
            if (millisSpent < minMillisSpent) {
                minMillisSpent = millisSpent;
            }
        }
        return minMillisSpent;
    }

    private static Map<GameKey, AggregateResult> loadAlreadyTestedGames(File outputCsvFile) throws IOException {
        List<CorrectnessTestResult> results = CsvFiles.load(outputCsvFile, CorrectnessTestResult.getCsvLoader());
        Map<GameKey, AggregateResult> groupedResults = Maps.newHashMap();
        for (CorrectnessTestResult result : results) {
            if (!groupedResults.containsKey(result.getGameKey())) {
                groupedResults.put(result.getGameKey(), new AggregateResult(result));
            } else {
                groupedResults.get(result.getGameKey()).foldIn(result);
            }
        }
        return groupedResults;
    }

    //WARNING: mutable, not thread-safe
    private static class AggregateResult {
        private final GameKey gameKey;
        private long millisSpentSoFar;
        private int mostStateChangesSoFar;
        private boolean failure;

        public AggregateResult(CorrectnessTestResult result) {
            this.gameKey = result.getGameKey();
            this.millisSpentSoFar = result.getMillisecondsTaken();
            this.mostStateChangesSoFar = result.getNumStateChanges();
            this.failure = result.getError().isPresent();
        }

        public void foldIn(CorrectnessTestResult result) {
            this.millisSpentSoFar += result.getMillisecondsTaken();
            this.mostStateChangesSoFar = Math.max(this.mostStateChangesSoFar, result.getNumStateChanges());
            this.failure |= result.getError().isPresent();
        }

        public GameKey getGameKey() {
            return gameKey;
        }

        public long getMillisSpentSoFar() {
            return millisSpentSoFar;
        }

        public int getMostStateChangesSoFar() {
            return mostStateChangesSoFar;
        }

        public boolean isFailure() {
            return failure;
        }
    }

    //TODO: Wrap all this in a big try block
    private static @Nullable CorrectnessTestResult runTest(int numStateChangesToTest,
            EngineType engineToTest,
            String version, JavaEngineType validationEngine, GameKey gameKey) throws Exception {
        Game game = gameKey.loadGame();
        File gameFile = File.createTempFile("game", ".kif");
        GameFiles.write(game, gameFile);
        File outputFile = File.createTempFile("history", ".log");
        Files.touch(outputFile);

        //TODO: Add right set of commands
        List<String> commands = Lists.newArrayList(engineToTest.getCommandsForCorrectnessTest());
        commands.add(gameFile.getAbsolutePath());
        commands.add(outputFile.getAbsolutePath());
        commands.add(Integer.toString(numStateChangesToTest));

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);
        Process process = pb.start();
        AtomicBoolean timedOut = new AtomicBoolean(false);

        TimeoutSignaler timeoutSignaler = new TimeoutSignaler();
        timeoutSignaler.onTimeoutDestroyForcibly(process);

        long startTime = System.currentTimeMillis();

        BlockingQueue<GameActionMessage> queue = Queues.newLinkedBlockingDeque(1000);
        Tailer tailer = Tailer.create(outputFile, new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                try {
                    Optional<GameActionMessage> action = GameActionParser.convertLine(line);
                    if (action.isPresent()) {
                        queue.put(action.get());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, 100);
        timeoutSignaler.onTimeout(tailer::stop);

        Callable<Optional<ObservedError>> validationCallable = () -> {
            return validationEngine.validateCorrectnessTestOutput(game, queue);
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Optional<ObservedError>> errorFuture = executor.submit(validationCallable);
        timeoutSignaler.onTimeoutShutdownNow(executor);

        CountDownLatch finishedSignal = new CountDownLatch(1);
        startTimeoutThread(timedOut, finishedSignal, timeoutSignaler, MAX_SECONDS_PER_TEST);
        try {
            process.waitFor();
            Optional<ObservedError> error = errorFuture.get();
            finishedSignal.countDown(); //cleans up stuff
            long timeTaken = System.currentTimeMillis() - startTime;
            if (timedOut.get()) {
                error = Optional.of(ObservedError.create("Timed out after " + MAX_SECONDS_PER_TEST + " seconds", 0));
                System.out.println("Recording timeout");
                return CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(version), validationEngine,
                        validationEngine.getVersion(), timeTaken, numStateChangesToTest, error);
            } else if (error == null) {
                System.out.println("No results; validation failed");
                return null;
            } else {
                if (error.isPresent()) {
                    numStateChangesToTest = error.get().getNumStateChangesBeforeFinding();
                }
                return CorrectnessTestResult.create(gameKey, engineToTest.getWithVersion(version), validationEngine,
                        validationEngine.getVersion(), timeTaken, numStateChangesToTest, error);
            }
        } finally {
            outputFile.delete();
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
