package net.alloyggp.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import net.alloyggp.perf.runner.JavaEngineType;

import org.ggp.base.util.game.Game;

import com.google.common.collect.Lists;

public class CorrectnessTestRunner {
	private static final EngineType ENGINE_TO_TEST = EngineType.COMPILED_PROVER;
	//To make things simpler, restrict validation to the Java engine types
	private static final JavaEngineType VALIDATION_ENGINE = JavaEngineType.PROVER;

	public static void main(String[] args) throws Exception {
		File outputCsvFile = getCsvOutputFileForEngine(ENGINE_TO_TEST);


		for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
			System.out.println("Testing game " + gameKey);
			CorrectnessTestResult result = runTest(ENGINE_TO_TEST, VALIDATION_ENGINE, gameKey);
			if (result != null) {
				CsvFiles.append(result, outputCsvFile);
			}
		}
	}

	public static File getCsvOutputFileForEngine(EngineType engineToTest) throws IOException {
		File outputDir = PerfTestRunner.getOutputDir("correctnessResults");
		File outputCsvFile = new File(outputDir, engineToTest.toString() + ".csv");
		outputCsvFile.createNewFile();
		return outputCsvFile;
	}

	private static CorrectnessTestResult runTest(EngineType engineToTest,
			JavaEngineType validationEngine, GameKey gameKey) throws Exception {
		Game game = gameKey.loadGame();
		File gameFile = File.createTempFile("game", ".kif");
		GameFiles.write(game, gameFile);

		//TODO: Add right set of commands
		List<String> commands = Lists.newArrayList(engineToTest.getCommandsForCorrectnessTest());

		ProcessBuilder pb = new ProcessBuilder();
		Process process = pb.start();
		Optional<ObservedError> error = null;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			error = validationEngine.validateCorrectnessTestOutput(game, in);
		} catch (Exception e) {
			//TODO: Better error handling?
			e.printStackTrace();
		} finally {
			process.destroyForcibly();
		}
		process.waitFor();
		if (error == null) {
			System.out.println("No results; validation failed");
			return null;
		} else {
			return CorrectnessTestResult.create(gameKey, engineToTest, validationEngine, error);
		}
	}

}
