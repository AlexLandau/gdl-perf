package net.alloyggp.perf.gameanalysis;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.io.Csvable;
import net.alloyggp.perf.io.CsvFiles.CsvLoadFunction;

public class GameAnalysisResult implements Csvable {
    private final GameKey gameKey;
    private final String key;
    private final String value;

    private GameAnalysisResult(GameKey gameKey, String key, String value) {
        Preconditions.checkArgument(!key.contains(getDelimiter()));
        Preconditions.checkArgument(!value.contains(getDelimiter()));
        this.gameKey = gameKey;
        this.key = key;
        this.value = value;
    }

    @Override
    public String getDelimiter() {
        return ";";
    }

    @Override
    public List<String> getValuesForCsv() {
        return ImmutableList.of(gameKey.toString(), key, value);
    }

    public GameKey getGameKey() {
        return gameKey;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static CsvLoadFunction<GameAnalysisResult> getCsvLoader() {
        return new CsvLoadFunction<GameAnalysisResult>() {
            @Override
            public GameAnalysisResult load(String inputLine) throws Exception {
                ImmutableList<String> strings = ImmutableList.copyOf(
                        Splitter.on(";").trimResults().split(inputLine));

                return create(
                        GameKey.create(strings.get(0)),
                        strings.get(1),
                        strings.get(2));
            }
        };
    }

    public static GameAnalysisResult create(GameKey gameKey, String key, String value) {
        return new GameAnalysisResult(gameKey, key, value);
    }

    public static Multimap<GameKey, GameAnalysisResult> groupByGame(List<GameAnalysisResult> allResults) {
        Multimap<GameKey, GameAnalysisResult> results = ArrayListMultimap.create();
        for (GameAnalysisResult result : allResults) {
            results.put(result.getGameKey(), result);
        }
        return results;
    }

}
