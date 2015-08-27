package net.alloyggp.perf;

import java.util.List;

import net.alloyggp.perf.io.CsvFiles.CsvLoadFunction;

import com.google.common.collect.ImmutableList;

public class InvalidGameResult implements Csvable {
    private final String gameKey;
    private final String errorMessage;

    private InvalidGameResult(String gameKey, String errorMessage) {
        this.gameKey = gameKey;
        this.errorMessage = errorMessage;
    }

    public static InvalidGameResult create(GameKey gameKey, String errorMessage) {
        return new InvalidGameResult(gameKey.toString(),
                errorMessage.replaceAll(";", ","));
    }

    public String getGameKey() {
        return gameKey;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getDelimiter() {
        return ";";
    }

    @Override
    public List<String> getValuesForCsv() {
        return ImmutableList.of(gameKey, errorMessage);
    }

    public static CsvLoadFunction<InvalidGameResult> getCsvLoader() {
        return line -> {
            String[] split = line.split(";");
            if (split.length != 2) {
                throw new IllegalArgumentException(line);
            }
            String gameKey = split[0];
            String errorMessage = split[1];
            return new InvalidGameResult(gameKey, errorMessage);
        };
    }

}
