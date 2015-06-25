package net.alloyggp.perf;

import java.util.List;

import net.alloyggp.perf.CsvFiles.CsvLoadFunction;

import com.google.common.collect.ImmutableList;

public class PerfTestResult implements Csvable {
	private final String gameKey;
	private final EngineType engineType;
	private final boolean successful;
	private final String errorMessage;
	private final long millisecondsTaken;
	private final long numStateChanges;
	private final long numRollouts;

	private PerfTestResult(String gameKey, EngineType engineType,
			boolean successful, String errorMessage,
			long millisecondsTaken, long numStateChanges, long numRollouts) {
		this.gameKey = gameKey;
		this.engineType = engineType;
		this.successful = successful;
		this.errorMessage = errorMessage.replaceAll(";", ",");
		this.millisecondsTaken = millisecondsTaken;
		this.numStateChanges = numStateChanges;
		this.numRollouts = numRollouts;
	}

	public static PerfTestResult createSuccess(GameKey gameKey,
			EngineType engineType, long millisecondsTaken, long numStateChanges,
			long numRollouts) {
		return new PerfTestResult(gameKey.toString(), engineType, true, "",
				millisecondsTaken, numStateChanges, numRollouts);
	}

	public static PerfTestResult createFailure(GameKey gameKey, EngineType engineType,
			String errorMessage) {
		return new PerfTestResult(gameKey.toString(), engineType, false, errorMessage, 0, 0, 0);
	}

	public String getGameKey() {
		return gameKey;
	}

	public EngineType getEngineType() {
		return engineType;
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
				engineType.toString(),
				Boolean.toString(successful),
				errorMessage,
				Long.toString(millisecondsTaken),
				Long.toString(numStateChanges),
				Long.toString(numRollouts));
	}

	public static CsvLoadFunction<PerfTestResult> getCsvLoader() {
		return line -> {
			String[] strings = line.split(";");
			if (strings.length != 7) {
				throw new IllegalArgumentException();
			}
			String gameKey = strings[0];
			EngineType engineType = EngineType.valueOf(strings[1]);
			boolean successful = Boolean.parseBoolean(strings[2]);
			String errorMessage = strings[3];
			long millisecondsTaken = Long.parseLong(strings[4]);
			long numStateChanges = Long.parseLong(strings[5]);
			long numRollouts = Long.parseLong(strings[6]);

			return new PerfTestResult(gameKey, engineType, successful,
					errorMessage, millisecondsTaken, numStateChanges, numRollouts);
		};
	}

}
