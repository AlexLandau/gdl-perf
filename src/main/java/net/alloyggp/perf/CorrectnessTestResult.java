package net.alloyggp.perf;

import java.util.List;
import java.util.Optional;

import net.alloyggp.perf.runner.JavaEngineType;

import com.google.common.collect.ImmutableList;

public class CorrectnessTestResult implements Csvable {
	private final GameKey gameKey;
	private final EngineType testedEngine;
	private final JavaEngineType referenceEngine;
	private final Optional<ObservedError> error;

	private CorrectnessTestResult(GameKey gameKey, EngineType testedEngine,
			JavaEngineType referenceEngine,
			Optional<ObservedError> error) {
		this.gameKey = gameKey;
		this.testedEngine = testedEngine;
		this.referenceEngine = referenceEngine;
		this.error = error;
	}

	public static CorrectnessTestResult create(GameKey gameKey,
			EngineType testedEngine, JavaEngineType referenceEngine,
			Optional<ObservedError> error) {
		return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine, error);
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
				+ ", error=" + error + "]";
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
				Boolean.toString(error.isPresent()),
				errorString.replaceAll(";", ","));
	}
}
