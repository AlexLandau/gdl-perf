package net.alloyggp.perf.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.ggp.base.util.Pair;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import net.alloyggp.perf.CorrectnessTestResult;
import net.alloyggp.perf.EngineVersion;
import net.alloyggp.perf.GameKey;
import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.analysis.html.HtmlAdHocTable;
import net.alloyggp.perf.analysis.html.HtmlPage;

public class InterlinkedAnalysisWriter {
    private final ImmutableSet<GameKey> allGameKeys;
    private final ImmutableSet<GameKey> validGameKeys;
    private final ImmutableMap<GameKey, String> gameFilenames;
    private final ImmutableList<PerfTestResult> allPerfResults;
    //TODO: Immutify these?
    private final Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame;
    private final Map<GameKey, List<EngineVersion>> rankingsByGame;
    private final ImmutableList<CorrectnessTestResult> allCorrectnessResults;
    private final ImmutableSet<EngineVersion> allEngines;
    private final ImmutableMap<EngineVersion, String> engineFilenames;

    private InterlinkedAnalysisWriter(ImmutableSet<GameKey> allGameKeys, ImmutableSet<GameKey> validGameKeys,
            ImmutableMap<GameKey, String> gameFilenames, ImmutableList<PerfTestResult> allPerfResults,
            Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame,
            Map<GameKey, List<EngineVersion>> rankingsByGame,
            ImmutableList<CorrectnessTestResult> allCorrectnessResults, ImmutableSet<EngineVersion> allEngines,
            ImmutableMap<EngineVersion, String> engineFilenames) {
        this.allGameKeys = allGameKeys;
        this.validGameKeys = validGameKeys;
        this.gameFilenames = gameFilenames;
        this.allPerfResults = allPerfResults;
        this.resultsByGame = resultsByGame;
        this.rankingsByGame = rankingsByGame;
        this.allCorrectnessResults = allCorrectnessResults;
        this.allEngines = allEngines;
        this.engineFilenames = engineFilenames;
    }

    private static InterlinkedAnalysisWriter create(Collection<GameKey> allGameKeys, Set<GameKey> validGameKeys,
            List<PerfTestResult> allPerfResults, Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame,
            List<CorrectnessTestResult> allCorrectnessResults, Set<EngineVersion> allEngines) {
        BiMap<GameKey, String> gameFilenames = getGameFilenames(allGameKeys);
        BiMap<EngineVersion, String> engineFilenames = getEngineFilenames(allEngines);
        return new InterlinkedAnalysisWriter(
                ImmutableSet.copyOf(allGameKeys),
                ImmutableSet.copyOf(validGameKeys),
                ImmutableMap.copyOf(gameFilenames),
                ImmutableList.copyOf(allPerfResults),
                resultsByGame,
                getEngineRankingsByGame(resultsByGame),
                ImmutableList.copyOf(allCorrectnessResults),
                ImmutableSet.copyOf(allEngines),
                ImmutableMap.copyOf(engineFilenames));
    }

    private static Map<GameKey, List<EngineVersion>> getEngineRankingsByGame(Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame) {
        Map<GameKey, List<EngineVersion>> rankings = Maps.newHashMap();
        for (GameKey game : resultsByGame.keySet()) {
            List<PerfTestResult> successfulResultsToSort = Lists.newArrayList();
            Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
            for (Entry<EngineVersion, PerfTestResult> entry : resultsByEngine.entrySet()) {
                EngineVersion engine = entry.getKey();
                PerfTestResult result = entry.getValue();
                if (result.wasSuccessful()) {
                    successfulResultsToSort.add(result);
                }
            }
            successfulResultsToSort.sort(
                    Comparator.comparing((PerfTestResult result) ->
                    result.getNumStateChanges() / (double) result.getMillisecondsTaken())
                    .reversed()); // values should be descending
            List<EngineVersion> ranking = successfulResultsToSort.stream()
                    .map(PerfTestResult::getEngineVersion)
                    .collect(Collectors.toList());
            rankings.put(game, ranking);
        }
        return rankings;
    }


    private static BiMap<EngineVersion, String> getEngineFilenames(Set<EngineVersion> engines) {
        BiMap<EngineVersion, String> results = HashBiMap.create();
        for (EngineVersion engine : engines) {
            //TODO: Add to escaping logic
            results.put(engine, engine.toString().replace(':', '_') + ".html");
        }
        return results;
    }

    private static BiMap<GameKey, String> getGameFilenames(Collection<GameKey> gameKeys) {
        BiMap<GameKey, String> results = HashBiMap.create();
        for (GameKey game : gameKeys) {
            //TODO: Add to escaping logic
            results.put(game, game.toString().replace('/', '_') + ".html");
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        Collection<GameKey> allGameKeys = GameKey.loadAllGameKeys();
        Set<GameKey> validGameKeys = GameKey.loadAllValidGameKeys();
        List<PerfTestResult> allPerfResults = PerfResultLoader.loadAllResults();
        List<CorrectnessTestResult> allCorrectnessResults = CorrectnessResultLoader.loadAllResults();
        Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame = PerfTestResult.groupByGameAndEngine(allPerfResults);
        Set<EngineVersion> allEngines = getAllEngines(allPerfResults, allCorrectnessResults);

        InterlinkedAnalysisWriter.create(allGameKeys,
                validGameKeys,
                allPerfResults,
                resultsByGame,
                allCorrectnessResults,
                allEngines).writeAnalyses();
    }

    private static Set<EngineVersion> getAllEngines(List<PerfTestResult> allPerfResults,
            List<CorrectnessTestResult> allCorrectnessResults) {
        Set<EngineVersion> results = Sets.newHashSet();
        for (PerfTestResult result : allPerfResults) {
            results.add(result.getEngineVersion());
        }
        for (CorrectnessTestResult result : allCorrectnessResults) {
            results.add(result.getTestedEngine());
        }
        return results;
    }

    private void writeAnalyses() throws IOException {
        for (GameKey game : allGameKeys) {
            String htmlFilename = gameFilenames.get(game);
            writeHtmlPage(htmlFilename, getGamePage(game));
        }

        for (EngineVersion engine : allEngines) {
            String htmlFilename = engineFilenames.get(engine);
            writeHtmlPage(htmlFilename, getEnginePage(engine));
        }
    }

    private void writeHtmlPage(String htmlFilename, HtmlPage gamePage) throws IOException {
        File outputDir = new File("analyses");
        outputDir.mkdirs();
        Files.write(gamePage.toHtml(), new File(outputDir, htmlFilename), Charsets.UTF_8);
    }

    private HtmlPage getGamePage(GameKey game) {
        HtmlPage page = HtmlPage.create("Game " + game);
        page.addHeader(game.toString());
        if (validGameKeys.contains(game)) {
            //Add results relating to game
            Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
            if (resultsByEngine == null) {
                page.addText("No perf results found for game.");
            } else {
                Map<EngineVersion, PerfTestResult> successfulResults = Maps.filterValues(resultsByEngine, result -> result.wasSuccessful());
                Map<EngineVersion, PerfTestResult> resultsWithErrors = Maps.filterValues(resultsByEngine, result -> !result.wasSuccessful());
                if (!successfulResults.isEmpty()) {
                    SortedSet<Pair<EngineVersion, PerfTestResult>> resultPairs = Sets.newTreeSet(
                            Comparator.comparing((Pair<EngineVersion, PerfTestResult> pair) -> {
                                PerfTestResult result = pair.right;
                                return result.getNumStateChanges() / (double) result.getMillisecondsTaken();
                            }).reversed());
                    for (Entry<EngineVersion, PerfTestResult> entry : successfulResults.entrySet()) {
                        resultPairs.add(Pair.from(entry));
                    }
                    HtmlAdHocTable engineTable = HtmlAdHocTable.create();
                    for (Pair<EngineVersion, PerfTestResult> resultPair : resultPairs) {
                        engineTable.addRow(link(resultPair.left), getAvg(resultPair.right));
                    }
                    page.add(engineTable);
                }
                if (!resultsWithErrors.isEmpty()) {
                    page.addText("Machines with errors:");
                    HtmlAdHocTable errorTable = HtmlAdHocTable.create();
                    for (Entry<EngineVersion, PerfTestResult> entry : resultsWithErrors.entrySet()) {
                        errorTable.addRow(link(entry.getKey()), entry.getValue().getErrorMessage());
                    }
                    page.add(errorTable);
                }
            }
        } else {
            page.addText("Game is considered invalid.");
        }
        return page;
    }

    private String getAvg(PerfTestResult result) {
    	if (result.getMillisecondsTaken() == 0) {
    		return "error";
    	}
        return Long.toString(getAvgNum(result));
    }

    private long getAvgNum(PerfTestResult result) {
        return (result.getNumStateChanges() * 1000L) / result.getMillisecondsTaken();
    }

    private String link(EngineVersion engine) {
        String filename = engineFilenames.get(engine);
        return "<a href=\""+filename+"\">"+engine.toString()+"</a>";
    }

    private String link(GameKey game) {
        String filename = gameFilenames.get(game);
        return "<a href=\""+filename+"\">"+game.toString()+"</a>";
    }

    private HtmlPage getEnginePage(EngineVersion engine) throws IOException {
        HtmlPage page = HtmlPage.create("Perf results for " + engine.toString());
        page.addHeader(engine.toString());
        //Add results...
        { //TODO: Factor into method
            ProverComparisonPerfSummary proverComparison = ProverComparisonPerfSummary.create(engine);
            page.addText("Comparison with GGP-Base Prover:");
            HtmlAdHocTable comparisonTable = HtmlAdHocTable.create();
            comparisonTable.addRow("Median speedup", proverComparison.getMedianSpeedup()+"");
            comparisonTable.addRow("Mean speedup", proverComparison.getMeanSpeedup()+"");
            comparisonTable.addRow("Geometric mean speedup", proverComparison.getSpeedupGeometricMean()+"");
            comparisonTable.addRow("Ratio of # state changes with 1 second per game", proverComparison.getNumStateChangesWith1SecondPerGameRatio()+"");
            comparisonTable.addRow("Ratio of times to run 1000 states per game", proverComparison.getTimeToRun1000StatesPerGameRatio()+"");
            comparisonTable.addRow("Number of games with errors", proverComparison.getNumGamesWithErrors()+"");

            page.add(comparisonTable);
        }
        //Now a list of games
        //Ideally, we list game -> avg. speed -> ranking among successful engines
        {
            HtmlAdHocTable gameTable = HtmlAdHocTable.create();
            gameTable.addRow("Game", "Average state changes per second", "Ranking");
            HtmlAdHocTable errorsTable = HtmlAdHocTable.create();
            errorsTable.addRow("Game", "Error message");
            boolean anyErrorsFound = false;
            List<PerfTestResult> resultsToSort = Lists.newArrayList();
            for (GameKey game : validGameKeys) {
                PerfTestResult result = resultsByGame.get(game).get(engine);
                if (result != null) {
                	if (result.wasSuccessful()) {
                	    resultsToSort.add(result);
                	} else {
                		errorsTable.addRow(link(game), result.getErrorMessage());
                		anyErrorsFound = true;
                	}
                }
            }
            resultsToSort.sort(Comparator.comparing(this::getAvgNum).reversed());
            for (PerfTestResult result : resultsToSort) {
                GameKey game = result.getGameKey();
                int ranking = rankingsByGame.get(game).indexOf(engine) + 1;
                gameTable.addRow(link(game), getAvg(result), ranking+"");
            }
            page.addText("Game-by-game statistics:");
            page.add(gameTable);
            if (anyErrorsFound) {
                page.addText("Games with errors:");
                page.add(errorsTable);
            }
        }

        return page;
    }

}
