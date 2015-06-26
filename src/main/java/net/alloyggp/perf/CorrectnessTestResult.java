package net.alloyggp.perf;

import java.util.List;
import java.util.Optional;

import net.alloyggp.perf.CsvFiles.CsvLoadFunction;
import net.alloyggp.perf.runner.JavaEngineType;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class CorrectnessTestResult implements Csvable {
	private final GameKey gameKey;
	private final EngineType testedEngine;
	private final JavaEngineType referenceEngine;
	private final int numStateChanges;
	private final Optional<ObservedError> error;

	private CorrectnessTestResult(GameKey gameKey, EngineType testedEngine,
			JavaEngineType referenceEngine,
			int numStateChanges,
			Optional<ObservedError> error) {
		this.gameKey = gameKey;
		this.testedEngine = testedEngine;
		this.referenceEngine = referenceEngine;
		this.numStateChanges = numStateChanges;
		this.error = error;
	}

	public static CorrectnessTestResult create(GameKey gameKey,
			EngineType testedEngine, JavaEngineType referenceEngine,
			int numStateChanges, Optional<ObservedError> error) {
		return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine, numStateChanges, error);
	}

	public GameKey getGameKey() {
		return gameKey;
	}

	public EngineType getTestedEngine() {
		return testedEngine;
	}

	public JavaEngineType getReferenceEngine() {
		return referenceEngine;
	}

	public Optional<ObservedError> getError() {
		return error;
	}

	@Override
	public String toString() {
		return "CorrectnessTestResult [gameKey=" + gameKey + ", testedEngine="
				+ testedEngine + ", referenceEngine=" + referenceEngine
				+ ", numStateChanges=" + numStateChanges + ", error=" + error
				+ "]";
	}

	@Override
	public String getDelimiter() {
		return ";";
	}

	@Override
	public List<String> getValuesForCsv() {
		String errorString = error.isPresent() ? error.get().toString() : "";
		return ImmutableList.of(gameKey.toString(),
				testedEngine.toString(),
				referenceEngine.toString(),
				Integer.toString(numStateChanges),
				Boolean.toString(error.isPresent()),
				errorString.replaceAll(";", ",")
							.replaceAll("\n", "    "));
	}

	public static CsvLoadFunction<CorrectnessTestResult> getCsvLoader() {
		return line -> {
			List<String> split = ImmutableList.copyOf(Splitter.on(";").split(line));
			Preconditions.checkArgument(split.size() == 6);
			GameKey gameKey = GameKey.create(split.get(0));
			EngineType testedEngine = EngineType.valueOf(split.get(1));
			JavaEngineType referenceEngine = JavaEngineType.valueOf(split.get(2));
			int numStateChanges = Integer.parseInt(split.get(3));
			boolean errorPresent = Boolean.parseBoolean(split.get(4));
			Optional<ObservedError> error;
			//Slightly hacky for now; not a complete reproduction of internal state
			if (errorPresent) {
				error = Optional.of(ObservedError.create(split.get(5), numStateChanges));
			} else {
				error = Optional.empty();
			}

			return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine, numStateChanges, error);
		};
	}
}
