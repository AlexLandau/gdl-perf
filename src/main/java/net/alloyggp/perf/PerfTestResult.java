package net.alloyggp.perf;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.alloyggp.perf.engine.EngineType;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.io.Csvable;
import net.alloyggp.perf.io.CsvFiles.CsvLoadFunction;

//TODO: Record time allotted length?
public class PerfTestResult implements Csvable {
    private final GameKey gameKey;
    private final EngineVersion engineVersion;
    private final boolean successful;
    private final String errorMessage;
    private final long millisecondsTaken;
    private final long numStateChanges;
    private final long numRollouts;

    private PerfTestResult(GameKey gameKey, EngineVersion engineVersion,
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
            EngineType type, String version, long millisecondsTaken, long numStateChanges,
            long numRollouts) {
        return createSuccess(gameKey, EngineVersion.create(type, version),
                millisecondsTaken, numStateChanges, numRollouts);
    }

    public static PerfTestResult createSuccess(GameKey gameKey,
            EngineVersion engine, long millisecondsTaken, long numStateChanges,
            long numRollouts) {
        return new PerfTestResult(gameKey, engine, true, "",
                millisecondsTaken, numStateChanges, numRollouts);
    }

    public static PerfTestResult createFailure(GameKey gameKey, EngineVersion engine,
            String errorMessage) {
        return new PerfTestResult(gameKey, engine, false, errorMessage, 0, 0, 0);
    }

    public GameKey getGameKey() {
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
                gameKey.toString(),
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
            GameKey gameKey = GameKey.create(split.get(0));
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

    public static Map<GameKey, Map<EngineVersion, PerfTestResult>> groupByGameAndEngine(
            Collection<PerfTestResult> results) {
        Map<GameKey, Map<EngineVersion, PerfTestResult>> map = Maps.newHashMap();
        for (PerfTestResult result : results) {
            EngineVersion engineVersion = result.getEngineVersion();
            GameKey game = result.getGameKey();
            if (!map.containsKey(game)) {
                Map<EngineVersion, PerfTestResult> newMap = Maps.newHashMap();
                newMap.put(engineVersion, result);
                map.put(game, newMap);
            } else {
                Map<EngineVersion, PerfTestResult> innerMap = map.get(game);
                if (!innerMap.containsKey(engineVersion)) {
                    innerMap.put(engineVersion, result);
                } else {
                    PerfTestResult existingResult = innerMap.get(engineVersion);
                    innerMap.put(engineVersion, mergeResults(result, existingResult));
                }
            }
        }
        return map;
    }

    public static Map<GameKey, PerfTestResult> groupByGameSingleEngineVersion(
            Collection<PerfTestResult> results) {
        Map<GameKey, PerfTestResult> map = Maps.newHashMap();
        for (PerfTestResult result : results) {
            GameKey game = result.getGameKey();
            if (!map.containsKey(game)) {
                map.put(game, result);
            } else {
                PerfTestResult existingResult = map.get(game);
                map.put(game, mergeResults(result, existingResult));
            }
        }
        return map;
    }

    private static PerfTestResult mergeResults(PerfTestResult result1,
            PerfTestResult result2) {
        if (!result1.gameKey.equals(result2.gameKey)) {
            throw new IllegalArgumentException("Game keys are not the same");
        }
        if (!result1.engineVersion.equals(result2.engineVersion)) {
            throw new IllegalArgumentException("Engine versions are not the same");
        }
        if (!result1.wasSuccessful()) {
            return result1;
        }
        if (!result2.wasSuccessful()) {
            return result2;
        }
        //Merge the stats

        return createSuccess(result1.gameKey, result1.engineVersion,
                result1.millisecondsTaken + result2.millisecondsTaken,
                result1.numStateChanges + result2.numStateChanges,
                result1.numRollouts + result2.numRollouts);
    }

}
