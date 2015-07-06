package net.alloyggp.perf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.alloyggp.perf.io.CsvFiles;

import com.google.common.collect.Maps;

public class InvalidGames {
	private InvalidGames() {
		//Not instantiable
	}


	public static File getInvalidGamesFile() throws IOException {
		File invalidGamesFile = new File("invalidGames.csv");
		invalidGamesFile.createNewFile(); //in case it doesn't exist
		return invalidGamesFile;
	}

	public static Map<GameKey, String> loadInvalidGames() throws IOException {
		Map<GameKey, String> results = Maps.newHashMap();
		List<InvalidGameResult> invalidGames = CsvFiles.load(getInvalidGamesFile(), InvalidGameResult.getCsvLoader());
		for (InvalidGameResult invalidGame : invalidGames) {
			results.put(GameKey.create(invalidGame.getGameKey()), invalidGame.getErrorMessage());
		}
		return results;
	}

	public static void recordValidationFailure(InvalidGameResult invalidGameResult) throws IOException {
		CsvFiles.append(invalidGameResult, getInvalidGamesFile());
	}
}
