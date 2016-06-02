package net.alloyggp.perf.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.game.GameKey;

public class PlaceCountTableMaker {

    public static Table<String, String, String> create() throws IOException {

        //Place is first (going down), machine second (going across), number of games in the cell
        Table<String, String, String> table = HashBasedTable.create();
        Map<EngineVersion, Multimap<String, GameKey>> placesByEngine = computePlacesByEngine();

        //Now turn placesByEngine into table
        for (EngineVersion engine : placesByEngine.keySet()) {
            Multimap<String, GameKey> places = placesByEngine.get(engine);
            for (String place : places.keySet()) {
                Collection<GameKey> gameKeys = places.get(place);
                int count = gameKeys.size();
                List<GameKey> randomSample = getRandomSample(gameKeys);
                String cellEntry = "<b>" + count + "</b><div style=\"font-size:75%\">";
                for (GameKey key : randomSample) {
                    cellEntry += "\n<br/>" + key;
                }
                cellEntry += "</div>";
                table.put(place, engine.toString(), cellEntry);
            }
        }
        return table;
    }

    private static final Random RAND = new Random();
    private static List<GameKey> getRandomSample(Collection<GameKey> gameKeys) {
        List<GameKey> toReturn = Lists.newArrayList();
        List<GameKey> indexed = Lists.newArrayList(gameKeys);
        while (toReturn.size() < 5 && !indexed.isEmpty()) {
            int chosenIndex = RAND.nextInt(indexed.size());
            toReturn.add(indexed.get(chosenIndex));
            indexed.remove(chosenIndex);
        }
        toReturn.sort(Comparator.comparing(GameKey::toString));
        return toReturn;
    }

    //TODO: Use a Multimap<String, GameKey> in place of the Multiset<String> so we can include
    //a random sample of games in each cell
    public static Map<EngineVersion, Multimap<String, GameKey>> computePlacesByEngine() throws IOException {
        List<PerfTestResult> allResults = PerfResultLoader.loadAllResults();
        Map<EngineVersion, Multimap<String, GameKey>> placesByEngine = Maps.newHashMap();

        //Get the places
        Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame = PerfTestResult.groupByGameAndEngine(allResults);
        for (GameKey game : resultsByGame.keySet()) {
            List<PerfTestResult> successfulResultsToSort = Lists.newArrayList();
            Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
            for (Entry<EngineVersion, PerfTestResult> entry : resultsByEngine.entrySet()) {
                EngineVersion engine = entry.getKey();
                if (!placesByEngine.containsKey(engine)) {
                    placesByEngine.put(engine, HashMultimap.create());
                }
                PerfTestResult result = entry.getValue();
                if (!result.wasSuccessful()) {
                    placesByEngine.get(engine).put("error", result.getGameKey());
                } else {
                    successfulResultsToSort.add(result);
                }
            }
            successfulResultsToSort.sort(
                    Comparator.comparing((PerfTestResult result) ->
                            result.getNumStateChanges() / (double) result.getMillisecondsTaken())
                    .reversed()); // values should be descending
            for (int i = 0; i < successfulResultsToSort.size(); i++) {
                PerfTestResult result = successfulResultsToSort.get(i);
                EngineVersion engineVersion = result.getEngineVersion();
                String place = Integer.toString(i + 1);
                placesByEngine.get(engineVersion).put(place, result.getGameKey());
            }
        }
        return placesByEngine;
    }
}
