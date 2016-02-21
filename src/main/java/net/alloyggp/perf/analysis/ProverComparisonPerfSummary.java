package net.alloyggp.perf.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.alloyggp.perf.EngineType;
import net.alloyggp.perf.EngineVersion;
import net.alloyggp.perf.GameKey;
import net.alloyggp.perf.InvalidGames;
import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.runner.JavaEngineType;

public class ProverComparisonPerfSummary {
    private final int numGamesInResults;
    private final int numGamesWithErrors;
    private final double timeToRun1000StatesPerGameRatio;
    private final double numStateChangesWith1SecondPerGameRatio;
    private final double meanSpeedup;
    private final double medianSpeedup;
    private final double speedupGeometricMean;

    private ProverComparisonPerfSummary(int numGamesInResults,
            int numGamesWithErrors, double timeToRun1000StatesPerGameRatio,
            double numStateChangesWith1SecondPerGameRatio, double meanSpeedup,
            double medianSpeedup, double speedupGeometricMean) {
        this.numGamesInResults = numGamesInResults;
        this.numGamesWithErrors = numGamesWithErrors;
        this.timeToRun1000StatesPerGameRatio = timeToRun1000StatesPerGameRatio;
        this.numStateChangesWith1SecondPerGameRatio = numStateChangesWith1SecondPerGameRatio;
        this.meanSpeedup = meanSpeedup;
        this.medianSpeedup = medianSpeedup;
        this.speedupGeometricMean = speedupGeometricMean;
    }

    public int getNumGamesInResults() {
        return numGamesInResults;
    }

    public int getNumGamesWithErrors() {
        return numGamesWithErrors;
    }

    public double getTimeToRun1000StatesPerGameRatio() {
        return timeToRun1000StatesPerGameRatio;
    }

    public double getNumStateChangesWith1SecondPerGameRatio() {
        return numStateChangesWith1SecondPerGameRatio;
    }

    public double getMeanSpeedup() {
        return meanSpeedup;
    }

    public double getMedianSpeedup() {
        return medianSpeedup;
    }

    public double getSpeedupGeometricMean() {
        return speedupGeometricMean;
    }

    public static ProverComparisonPerfSummary create(EngineVersion engineVersion) throws IOException {
        EngineVersion proverVersion = EngineType.GGP_BASE_PROVER.getWithVersion(JavaEngineType.GGP_BASE_PROVER.getVersion());
        //Load results, find set of games where neither have errors

        Set<GameKey> invalidGames = InvalidGames.loadInvalidGames().keySet();
        Map<GameKey, PerfTestResult> proverResultsMap = loadValidGamePerfResults(proverVersion, invalidGames);
        Map<GameKey, PerfTestResult> engineResultsMap = loadValidGamePerfResults(engineVersion, invalidGames);

        Set<GameKey> gamesWithoutErrors = Sets.newHashSet();
        gamesWithoutErrors.addAll(Sets.intersection(proverResultsMap.keySet(), engineResultsMap.keySet()));
        for (PerfTestResult result : Iterables.concat(proverResultsMap.values(),
                                                      engineResultsMap.values())) {
            if (!result.wasSuccessful()) {
                gamesWithoutErrors.remove(result.getGameKey());
            }
        }

        SingleEnginePerfSummary proverSummary = SingleEnginePerfSummary.create(gamesWithoutErrors, proverVersion, proverResultsMap.values());
        SingleEnginePerfSummary engineSummary = SingleEnginePerfSummary.create(gamesWithoutErrors, engineVersion, engineResultsMap.values());
        Preconditions.checkState(gamesWithoutErrors.equals(proverSummary.getGamesInvolved()));
        Preconditions.checkState(gamesWithoutErrors.equals(engineSummary.getGamesInvolved()));

        Map<GameKey, Double> speedups = Maps.newHashMap();
        DescriptiveStatistics speedupStatistics = new DescriptiveStatistics();
        for (GameKey game : gamesWithoutErrors) {
            double speedup = engineSummary.getAverageStatesPerSecond().get(game)
                           / proverSummary.getAverageStatesPerSecond().get(game);
            speedups.put(game, speedup);
            speedupStatistics.addValue(speedup);
        }

        return new ProverComparisonPerfSummary(gamesWithoutErrors.size(),
                engineResultsMap.size() - gamesWithoutErrors.size(),
                engineSummary.getTimeToRun1000StatesPerGame() / proverSummary.getTimeToRun1000StatesPerGame(),
                engineSummary.getNumStateChangesWith1Second() / proverSummary.getNumStateChangesWith1Second(),
                speedupStatistics.getMean(),
                speedupStatistics.getPercentile(50),
                speedupStatistics.getGeometricMean());
    }

    public static Map<GameKey, PerfTestResult> loadValidGamePerfResults(EngineVersion proverVersion, Set<GameKey> invalidGames)
            throws IOException {
        List<PerfTestResult> allProverResults = PerfResultLoader.loadAllResults(proverVersion);
        Map<GameKey, PerfTestResult> proverResultsMap = PerfTestResult.groupByGameSingleEngineVersion(allProverResults);
        proverResultsMap = Maps.filterKeys(proverResultsMap, Predicates.not(invalidGames::contains));
        return proverResultsMap;
    }

    public static class SingleEnginePerfSummary {
        private final EngineVersion engine;
        private final ImmutableSet<GameKey> gamesInvolved;
        private final double timeToRun1000StatesPerGame;
        private final ImmutableMap<GameKey, Double> averageStatesPerSecond;
        private final double numStateChangesWith1Second;

        private SingleEnginePerfSummary(EngineVersion engine,
                ImmutableSet<GameKey> gamesInvolved,
                double timeToRun1000StatesPerGame,
                ImmutableMap<GameKey, Double> averageStatesPerSecond,
                double numStateChangesWith1Second) {
            this.engine = engine;
            this.gamesInvolved = gamesInvolved;
            this.timeToRun1000StatesPerGame = timeToRun1000StatesPerGame;
            this.averageStatesPerSecond = averageStatesPerSecond;
            this.numStateChangesWith1Second = numStateChangesWith1Second;
        }

        public static SingleEnginePerfSummary create(Set<GameKey> gamesToUse,
                EngineVersion engine, Collection<PerfTestResult> results) {
            results = Collections2.filter(results, result -> result.getEngineVersion().equals(engine));
            Map<GameKey, PerfTestResult> resultsByGame = PerfTestResult.groupByGameSingleEngineVersion(results);

            Set<GameKey> gamesWithErrors = Sets.newHashSet();
            Map<GameKey, Double> averageStatesPerSecond = Maps.newHashMap();
            double timeToRun1000StatesPerGame = 0.0;
            double numStateChangesWith1Second = 0.0;
            for (GameKey game : gamesToUse) {
                PerfTestResult result = resultsByGame.get(game);
                if (!result.wasSuccessful()) {
                    gamesWithErrors.add(game);
                    continue;
                }

                double statesPerSecond = (1000.0 * result.getNumStateChanges()) / result.getMillisecondsTaken();
                averageStatesPerSecond.put(game, statesPerSecond);
                timeToRun1000StatesPerGame += (1000 / statesPerSecond);
                numStateChangesWith1Second += statesPerSecond;
            }

            return new SingleEnginePerfSummary(engine,
                    ImmutableSet.copyOf(Sets.difference(gamesToUse, gamesWithErrors)),
                    timeToRun1000StatesPerGame,
                    ImmutableMap.copyOf(averageStatesPerSecond),
                    numStateChangesWith1Second);
        }

        public EngineVersion getEngine() {
            return engine;
        }

        public ImmutableSet<GameKey> getGamesInvolved() {
            return gamesInvolved;
        }

        public double getTimeToRun1000StatesPerGame() {
            return timeToRun1000StatesPerGame;
        }

        public ImmutableMap<GameKey, Double> getAverageStatesPerSecond() {
            return averageStatesPerSecond;
        }

        public double getNumStateChangesWith1Second() {
            return numStateChangesWith1Second;
        }

    }

    @Override
    public String toString() {
        return "ProverComparisonPerfSummary [numGamesInResults="
                + numGamesInResults + ", numGamesWithErrors="
                + numGamesWithErrors + ", timeToRun1000StatesPerGameRatio="
                + timeToRun1000StatesPerGameRatio
                + ", numStateChangesWith1SecondPerGameRatio="
                + numStateChangesWith1SecondPerGameRatio + ", meanSpeedup="
                + meanSpeedup + ", medianSpeedup=" + medianSpeedup
                + ", speedupGeometricMean=" + speedupGeometricMean + "]";
    }
}
