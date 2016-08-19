package net.alloyggp.perf.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import net.alloyggp.perf.correctness.CorrectnessTestResult;
import net.alloyggp.perf.engine.EngineVersion;
import net.alloyggp.perf.game.GameKey;

public class CorrectnessSkim {

    public static void main(String[] args) throws IOException {
        Multimap<EngineVersion, CorrectnessTestResult> resultsMap = CorrectnessResultLoader.loadResultsMap();

        Set<GameKey> gamesToConsider = GameKey.loadAllValidGameKeys();
        resultsMap = Multimaps.filterValues(resultsMap, result -> gamesToConsider.contains(result.getGameKey()));

        System.out.println("Number of games considered: " + gamesToConsider.size());
        for (EngineVersion ev : resultsMap.keySet()) {
            System.out.println("For engine " + ev.toString() + ":");
            Multimap<GameKey, CorrectnessTestResult> resultsByGame = CorrectnessTestResult.groupByGame(resultsMap.get(ev));
            int successfulSoFarCount = 0;
            int untestedCount = 0;
            int errorsFoundCount = 0;
            Set<GameKey> gamesWithErrors = Sets.newHashSet();

            for (GameKey game : gamesToConsider) {
                Collection<CorrectnessTestResult> resultsForGame = resultsByGame.get(game);
                if (resultsForGame.isEmpty()) {
                    untestedCount++;
                } else if (resultsForGame.stream().anyMatch(result -> result.getError().isPresent())) {
                    errorsFoundCount++;
                    gamesWithErrors.add(game);
                } else {
                    successfulSoFarCount++;
                }
            }

            System.out.println("  Successful so far: " + successfulSoFarCount);
            System.out.println("  Untested: " + untestedCount);
            System.out.println("  Errors: " + errorsFoundCount);
        }
    }

}
