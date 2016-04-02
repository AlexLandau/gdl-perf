package net.alloyggp.perf.correctness;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import net.alloyggp.perf.ObservedError;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.io.Csvable;
import net.alloyggp.perf.io.CsvFiles.CsvLoadFunction;
import net.alloyggp.perf.runner.JavaEngineType;

//TODO: Explicitly treat timeouts differently, record time allotted length
public class CorrectnessTestResult implements Csvable {
    private final GameKey gameKey;
    private final EngineVersion testedEngine;
    private final JavaEngineType referenceEngine;
    private final String referenceEngineVersion;
    private final long millisecondsTaken;
    private final int numStateChanges;
    private final Optional<ObservedError> error;

    private CorrectnessTestResult(GameKey gameKey, EngineVersion testedEngine,
            JavaEngineType referenceEngine, String referenceEngineVersion,
            long millisecondsTaken,
            int numStateChanges,
            Optional<ObservedError> error) {
        this.gameKey = gameKey;
        this.testedEngine = testedEngine;
        this.referenceEngine = referenceEngine;
        this.referenceEngineVersion = referenceEngineVersion;
        this.millisecondsTaken = millisecondsTaken;
        this.numStateChanges = numStateChanges;
        this.error = error;
    }

    public static CorrectnessTestResult create(GameKey gameKey,
            EngineVersion testedEngine, JavaEngineType referenceEngine,
            String referenceEngineVersion,
            long millisecondsTaken,
            int numStateChanges, Optional<ObservedError> error) {
        return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine,
                referenceEngineVersion, millisecondsTaken, numStateChanges, error);
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

    public long getMillisecondsTaken() {
        return millisecondsTaken;
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
                Long.toString(millisecondsTaken),
                Integer.toString(numStateChanges),
                Boolean.toString(error.isPresent()),
                errorString.replaceAll(";", ",")
                .replaceAll("\n", "    ")
                .replaceAll("\r", ""));
    }

    public static CsvLoadFunction<CorrectnessTestResult> getCsvLoader() {
        return line -> {
            List<String> split = ImmutableList.copyOf(Splitter.on(";").split(line));
            Preconditions.checkArgument(split.size() == 9);
            GameKey gameKey = GameKey.create(split.get(0));
            EngineVersion testedEngine = EngineVersion.parse(split.get(1), split.get(2));
            JavaEngineType referenceEngine = JavaEngineType.valueOf(split.get(3));
            String referenceEngineVersion = split.get(4);
            long millisecondsTaken = Long.parseLong(split.get(5));
            int numStateChanges = Integer.parseInt(split.get(6));
            boolean errorPresent = Boolean.parseBoolean(split.get(7));
            Optional<ObservedError> error;
            //Slightly hacky for now; not a complete reproduction of internal state
            if (errorPresent) {
                error = Optional.of(ObservedError.create(split.get(8), numStateChanges));
            } else {
                error = Optional.empty();
            }

            return new CorrectnessTestResult(gameKey, testedEngine, referenceEngine,
                    referenceEngineVersion, millisecondsTaken, numStateChanges, error);
        };
    }
}
