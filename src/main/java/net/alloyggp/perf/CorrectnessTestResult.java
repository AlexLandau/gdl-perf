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
	private final EngineVersion testedEngine;
	private final JavaEngineType referenceEngine;
	private final String referenceEngineVersion;
	private final int numStateChanges;
	private final Optional<ObservedError> error;

	private CorrectnessTestResult(GameKey gameKey, EngineVersion testedEngine,
			JavaEngineType referenceEngine, String referenceEngineVersion,
			int numStateChanges,
			Optional<ObservedError> error) {
		this.gameKey = gameKey;
		this.testedEngine = testedEngine;
		this.referenceEngine = referenceEngine;
		this.referenceEngineVersion = referenceEngineVersion;
		this.numStateChanges = numStateChanges;
		this.error = error;
	}

	public static CorrectnessTestResult create(GameKey gameKey,
	        EngineVersion testedEngine, JavaEngineType referenceEngine,
	        String referenceEngineVersion,
			int numStateChanges, Optional<ObservedError> error) {
		return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine,
		        referenceEngineVersion, numStateChanges, error);
	}

	public GameKey getGameKey() {
		return gameKey;
	}

	public EngineVersion getTestedEngine() {
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
				testedEngine.getType().toString(),
				testedEngine.getVersion(),
				referenceEngine.toString(),
				referenceEngineVersion,
				Integer.toString(numStateChanges),
				Boolean.toString(error.isPresent()),
				errorString.replaceAll(";", ",")
							.replaceAll("\n", "    "));
	}

	public static CsvLoadFunction<CorrectnessTestResult> getCsvLoader() {
		return line -> {
			List<String> split = ImmutableList.copyOf(Splitter.on(";").split(line));
			Preconditions.checkArgument(split.size() == 8);
			GameKey gameKey = GameKey.create(split.get(0));
			EngineVersion testedEngine = EngineVersion.parse(split.get(1), split.get(2));
			JavaEngineType referenceEngine = JavaEngineType.valueOf(split.get(3));
			String referenceEngineVersion = split.get(4);
			int numStateChanges = Integer.parseInt(split.get(5));
			boolean errorPresent = Boolean.parseBoolean(split.get(6));
			Optional<ObservedError> error;
			//Slightly hacky for now; not a complete reproduction of internal state
			if (errorPresent) {
				error = Optional.of(ObservedError.create(split.get(7), numStateChanges));
			} else {
				error = Optional.empty();
			}

			return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine,
			        referenceEngineVersion, numStateChanges, error);
		};
	}
}
