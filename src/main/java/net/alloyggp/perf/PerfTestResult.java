package net.alloyggp.perf;

import java.util.List;

import net.alloyggp.perf.io.CsvFiles.CsvLoadFunction;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class PerfTestResult implements Csvable {
	private final String gameKey;
	private final EngineVersion engineVersion;
	private final boolean successful;
	private final String errorMessage;
	private final long millisecondsTaken;
	private final long numStateChanges;
	private final long numRollouts;

	private PerfTestResult(String gameKey, EngineVersion engineVersion,
			boolean successful, String errorMessage,
			long millisecondsTaken, long numStateChanges, long numRollouts) {
		this.gameKey = gameKey;
		this.engineVersion = engineVersion;
		this.successful = successful;
		this.errorMessage = errorMessage.replaceAll(";", ",");
		this.millisecondsTaken = millisecondsTaken;
		this.numStateChanges = numStateChanges;
		this.numRollouts = numRollouts;
	}

	public static PerfTestResult createSuccess(GameKey gameKey,
	        EngineVersion engine, long millisecondsTaken, long numStateChanges,
			long numRollouts) {
		return new PerfTestResult(gameKey.toString(), engine, true, "",
				millisecondsTaken, numStateChanges, numRollouts);
	}

	public static PerfTestResult createFailure(GameKey gameKey, EngineVersion engine,
			String errorMessage) {
		return new PerfTestResult(gameKey.toString(), engine, false, errorMessage, 0, 0, 0);
	}

	public String getGameKey() {
		return gameKey;
	}

	public EngineVersion getEngineVersion() {
		return engineVersion;
	}

	public boolean wasSuccessful() {
		return successful;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public long getMillisecondsTaken() {
		return millisecondsTaken;
	}

	public long getNumStateChanges() {
		return numStateChanges;
	}

	public long getNumRollouts() {
		return numRollouts;
	}

	@Override
	public String getDelimiter() {
		return ";";
	}

	@Override
	public List<String> getValuesForCsv() {
		return ImmutableList.of(
				gameKey,
				engineVersion.getType().toString(),
				engineVersion.getVersion().toString(),
				Boolean.toString(successful),
				errorMessage,
				Long.toString(millisecondsTaken),
				Long.toString(numStateChanges),
				Long.toString(numRollouts));
	}

	public static CsvLoadFunction<PerfTestResult> getCsvLoader() {
		return line -> {
		    List<String> split = ImmutableList.copyOf(Splitter.on(";").split(line));
			if (split.size() != 8) {
				throw new IllegalArgumentException();
			}
			String gameKey = split.get(0);
			EngineVersion engineVersion = EngineVersion.parse(split.get(1), split.get(2));
			boolean successful = Boolean.parseBoolean(split.get(3));
			String errorMessage = split.get(4);
			long millisecondsTaken = Long.parseLong(split.get(5));
			long numStateChanges = Long.parseLong(split.get(6));
			long numRollouts = Long.parseLong(split.get(7));

			return new PerfTestResult(gameKey, engineVersion, successful,
					errorMessage, millisecondsTaken, numStateChanges, numRollouts);
		};
	}

}
