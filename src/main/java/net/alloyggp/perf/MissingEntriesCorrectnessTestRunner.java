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

import net.alloyggp.perf.runner.GameActionMessage;
import net.alloyggp.perf.runner.GameActionParser;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.TimeoutSignaler;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlPool;

import com.google.common.collect.Lists;
import com.sun.istack.internal.Nullable;

public class MissingEntriesCorrectnessTestRunner {
	private static final EngineType ENGINE_TO_TEST = EngineType.PROVER;
	//To make things simpler, restrict validation to the Java engine types
	private static final JavaEngineType VALIDATION_ENGINE = JavaEngineType.PROVER;
	private static final int MIN_NUM_STATE_CHANGES_TO_TEST = 1000;
	private static final int MAX_SECONDS_PER_TEST = 240;

	public static void main(String[] args) throws Exception {
		File outputCsvFile = getCsvOutputFileForEngine(ENGINE_TO_TEST);

		Set<GameKey> alreadyTestedGames = loadAlreadyTestedGames(outputCsvFile);
		for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
			if (alreadyTestedGames.contains(gameKey)) {
				continue;
			}
			System.out.println("Testing game " + gameKey);
			try {
				CorrectnessTestResult result = runTest(ENGINE_TO_TEST, VALIDATION_ENGINE, gameKey);
				if (result != null) {
					CsvFiles.append(result, outputCsvFile);
				}
			} catch (Exception e) {
				ObservedError error = ObservedError.create(e.getMessage(), 0);
				CorrectnessTestResult result = CorrectnessTestResult.create(gameKey, ENGINE_TO_TEST, VALIDATION_ENGINE, 0, Optional.of(error));
				CsvFiles.append(result, outputCsvFile);
			}
			GdlPool.drainPool();
			System.gc();
		}
	}

	private static Set<GameKey> loadAlreadyTestedGames(File outputCsvFile) throws IOException {
		List<CorrectnessTestResult> results = CsvFiles.load(outputCsvFile, CorrectnessTestResult.getCsvLoader());
		return results.stream()
					  .map(CorrectnessTestResult::getGameKey)
					  .collect(Immutables.collectSet());
	}

	public static File getCsvOutputFileForEngine(EngineType engineToTest) throws IOException {
		File outputDir = PerfTestRunner.getOutputDir("correctnessResults");
		File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
		outputCsvFile.createNewFile();
		return outputCsvFile;
	}

	//TODO: Wrap all this in a big try block
	private static @Nullable CorrectnessTestResult runTest(EngineType engineToTest,
			JavaEngineType validationEngine, GameKey gameKey) throws Exception {
		Game game = gameKey.loadGame();
		File gameFile = File.createTempFile("game", ".kif");
		GameFiles.write(game, gameFile);

		//TODO: Add right set of commands
		List<String> commands = Lists.newArrayList(engineToTest.getCommandsForCorrectnessTest());
		commands.add(gameFile.getAbsolutePath());
		commands.add(Integer.toString(MIN_NUM_STATE_CHANGES_TO_TEST));

		ProcessBuilder pb = new ProcessBuilder(commands);
		Process process = pb.start();
		AtomicBoolean timedOut = new AtomicBoolean(false);

		TimeoutSignaler timeoutSignaler = new TimeoutSignaler();
		timeoutSignaler.onTimeoutDestroyForcibly(process);

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
		int numStateChanges = MIN_NUM_STATE_CHANGES_TO_TEST;
		if (timedOut.get()) {
			error = Optional.of(ObservedError.create("Timed out after " + MAX_SECONDS_PER_TEST + " seconds", 0));
			return CorrectnessTestResult.create(gameKey, engineToTest, validationEngine, numStateChanges, error);
		} else if (error == null) {
			System.out.println("No results; validation failed");
			return null;
		} else {
			if (error.isPresent()) {
				numStateChanges = error.get().getNumStateChangesBeforeFinding();
			}
			return CorrectnessTestResult.create(gameKey, engineToTest, validationEngine, numStateChanges, error);
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
