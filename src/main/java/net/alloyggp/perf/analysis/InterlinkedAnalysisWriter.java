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
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import net.alloyggp.perf.CorrectnessTestResult;
import net.alloyggp.perf.EngineVersion;
import net.alloyggp.perf.GameKey;
import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.analysis.html.HtmlAdHocTable;
import net.alloyggp.perf.analysis.html.HtmlPage;
import net.alloyggp.perf.game.GameAnalysisResult;
import net.alloyggp.perf.game.GameAnalysisResultLoader;

public class InterlinkedAnalysisWriter {
    private final ImmutableSet<GameKey> allGameKeys;
    private final ImmutableSet<GameKey> validGameKeys;
    private final ImmutableMap<GameKey, String> gameFilenames;
    private final ImmutableList<PerfTestResult> allPerfResults;
    //TODO: Immutify these?
    private final Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame;
    private final Map<GameKey, List<EngineVersion>> rankingsByGame;
    private final ImmutableList<CorrectnessTestResult> allCorrectnessResults;
    private final ImmutableMultimap<GameKey, GameAnalysisResult> perGameAnalysisResults;
    private final ImmutableSet<EngineVersion> allEngines;
    private final ImmutableMap<EngineVersion, String> engineFilenames;
    private final ImmutableMap<Set<EngineVersion>, String> enginePairFilenames;

    private InterlinkedAnalysisWriter(ImmutableSet<GameKey> allGameKeys, ImmutableSet<GameKey> validGameKeys,
            ImmutableMap<GameKey, String> gameFilenames, ImmutableList<PerfTestResult> allPerfResults,
            Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame,
            Map<GameKey, List<EngineVersion>> rankingsByGame,
            ImmutableList<CorrectnessTestResult> allCorrectnessResults,
            ImmutableMultimap<GameKey, GameAnalysisResult> perGameAnalysisResults, ImmutableSet<EngineVersion> allEngines,
            ImmutableMap<EngineVersion, String> engineFilenames,
            ImmutableMap<Set<EngineVersion>, String> enginePairFilenames) {
        this.allGameKeys = allGameKeys;
        this.validGameKeys = validGameKeys;
        this.gameFilenames = gameFilenames;
        this.allPerfResults = allPerfResults;
        this.resultsByGame = resultsByGame;
        this.rankingsByGame = rankingsByGame;
        this.allCorrectnessResults = allCorrectnessResults;
        this.perGameAnalysisResults = perGameAnalysisResults;
        this.allEngines = allEngines;
        this.engineFilenames = engineFilenames;
        this.enginePairFilenames = enginePairFilenames;
    }

    private static InterlinkedAnalysisWriter create(Collection<GameKey> allGameKeys, Set<GameKey> validGameKeys,
            List<PerfTestResult> allPerfResults, Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame,
            List<CorrectnessTestResult> allCorrectnessResults, Multimap<GameKey, GameAnalysisResult> perGameAnalysisResults,
            Set<EngineVersion> allEngines) {
        BiMap<GameKey, String> gameFilenames = getGameFilenames(allGameKeys);
        BiMap<EngineVersion, String> engineFilenames = getEngineFilenames(allEngines);
        BiMap<Set<EngineVersion>, String> enginePairFilenames = getEnginePairFilenames(allEngines);
        return new InterlinkedAnalysisWriter(
                ImmutableSet.copyOf(allGameKeys),
                ImmutableSet.copyOf(validGameKeys),
                ImmutableMap.copyOf(gameFilenames),
                ImmutableList.copyOf(allPerfResults),
                resultsByGame,
                getEngineRankingsByGame(resultsByGame),
                ImmutableList.copyOf(allCorrectnessResults),
                ImmutableMultimap.copyOf(perGameAnalysisResults),
                ImmutableSet.copyOf(allEngines),
                ImmutableMap.copyOf(engineFilenames),
                ImmutableMap.copyOf(enginePairFilenames));
    }

    private static Map<GameKey, List<EngineVersion>> getEngineRankingsByGame(Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame) {
        Map<GameKey, List<EngineVersion>> rankings = Maps.newHashMap();
        for (GameKey game : resultsByGame.keySet()) {
            List<PerfTestResult> successfulResultsToSort = Lists.newArrayList();
            Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
            for (PerfTestResult result : resultsByEngine.values()) {
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

    private static BiMap<GameKey, String> getGameFilenames(Collection<GameKey> gameKeys) {
        BiMap<GameKey, String> results = HashBiMap.create();
        for (GameKey game : gameKeys) {
            //TODO: Add to escaping logic
            results.put(game, game.toString().replace('/', '_') + ".html");
        }
        return results;
    }

    private static BiMap<EngineVersion, String> getEngineFilenames(Set<EngineVersion> engines) {
        BiMap<EngineVersion, String> results = HashBiMap.create();
        for (EngineVersion engine : engines) {
            //TODO: Add to escaping logic
            results.put(engine, engine.toString().replace(':', '_') + ".html");
        }
        return results;
    }

    private static BiMap<Set<EngineVersion>, String> getEnginePairFilenames(Set<EngineVersion> allEngines) {
        BiMap<Set<EngineVersion>, String> results = HashBiMap.create();
        for (EngineVersion engine1 : allEngines) {
            for (EngineVersion engine2 : allEngines) {
                //Only do one page per pair
                if (engine1.toString().compareTo(engine2.toString()) < 0) {
                    Set<EngineVersion> key = ImmutableSet.of(engine1, engine2);
                    String value = engine1 + "_vs_" + engine2;
                    results.put(key, value.replace(':', '_') + ".html");
                }
            }
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        Collection<GameKey> allGameKeys = GameKey.loadAllGameKeys();
        Set<GameKey> validGameKeys = GameKey.loadAllValidGameKeys();
        List<PerfTestResult> allPerfResults = PerfResultLoader.loadAllResults();
        List<CorrectnessTestResult> allCorrectnessResults = CorrectnessResultLoader.loadAllResults();
        Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame = ImmutableMap.copyOf(Maps.filterKeys(
                PerfTestResult.groupByGameAndEngine(allPerfResults),
                GameKey.loadAllValidGameKeys()::contains));
        Set<EngineVersion> allEngines = getAllEngines(allPerfResults, allCorrectnessResults);
        List<GameAnalysisResult> allGameAnalysisResults = GameAnalysisResultLoader.loadAllResults();
        Multimap<GameKey, GameAnalysisResult> perGameAnalysisResults = GameAnalysisResult.groupByGame(allGameAnalysisResults);

        InterlinkedAnalysisWriter.create(allGameKeys,
                validGameKeys,
                allPerfResults,
                resultsByGame,
                allCorrectnessResults,
                perGameAnalysisResults,
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

        for (EngineVersion engine1 : allEngines) {
            for (EngineVersion engine2 : allEngines) {
                //Only do one page per pair
                if (engine1.toString().compareTo(engine2.toString()) < 0) {
                    String htmlFilename = enginePairFilenames.get(ImmutableSet.of(engine1, engine2));
                    writeHtmlPage(htmlFilename, getEnginePairPage(engine1, engine2));
                }
            }
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
            //Add game statistics
            ImmutableCollection<GameAnalysisResult> analysisResults = perGameAnalysisResults.get(game);
            if (!analysisResults.isEmpty()) {
                HtmlAdHocTable statsTable = HtmlAdHocTable.create();
                for (GameAnalysisResult result : analysisResults) {
                    statsTable.addRow(result.getKey(), result.getValue());
                }
                page.add(statsTable);
            }

            //Add perf results relating to game
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
        return Double.toString(getAvgNum(result));
    }

    private double getAvgNum(PerfTestResult result) {
        return (result.getNumStateChanges() * 1000.0) / result.getMillisecondsTaken();
    }

    private String link(EngineVersion engine) {
        String filename = engineFilenames.get(engine);
        return link(engine.toString(), filename);
    }

    private String link(GameKey game) {
        String filename = gameFilenames.get(game);
        return link(game.toString(), filename);
    }

    private String linkComparison(String text, EngineVersion engine1, EngineVersion engine2) {
        String filename = enginePairFilenames.get(ImmutableSet.of(engine1, engine2));
        return link(text, filename);
    }

    private String link(String text, String filename) {
        return "<a href=\""+filename+"\">"+text+"</a>";
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
        //Comparisons with other engines
        {
            page.addText("Comparison with other engines:");
            HtmlAdHocTable table = HtmlAdHocTable.create();
            table.addRow("Engine", "We're faster in N games", "Only they fail", "They're faster in N games", "Only we fail");
            for (EngineVersion otherEngine : allEngines) {
                if (otherEngine.equals(engine)) {
                    continue;
                }
                int thisIsFasterCount = 0;
                int otherIsFasterCount = 0;
                int onlyTheyFail = 0;
                int onlyWeFail = 0;
                for (List<EngineVersion> ranking : rankingsByGame.values()) {
                    int ourIndex = ranking.indexOf(engine);
                    int theirIndex = ranking.indexOf(otherEngine);
                    if (ourIndex >= 0 && theirIndex >= 0) {
                        if (ourIndex < theirIndex) {
                            thisIsFasterCount++;
                        } else {
                            Preconditions.checkState(ourIndex > theirIndex);
                            otherIsFasterCount++;
                        }
                    } else if (ourIndex >= 0 && theirIndex == -1) {
                        onlyTheyFail++;
                    } else if (ourIndex == -1 && theirIndex >= 0) {
                        onlyWeFail++;
                    }
                }
                table.addRow(link(otherEngine) + " ("+linkComparison("comparison", engine, otherEngine)+")",
                        thisIsFasterCount+"", onlyTheyFail+"",
                        otherIsFasterCount+"", onlyWeFail+"");
            }
            //TODO: Sort table by contents?
            page.add(table);
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
                Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
                if (resultsByEngine != null) {
                    PerfTestResult result = resultsByEngine.get(engine);
                    if (result != null) {
                        if (result.wasSuccessful()) {
                            resultsToSort.add(result);
                        } else {
                            errorsTable.addRow(link(game), result.getErrorMessage());
                            anyErrorsFound = true;
                        }
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

    private HtmlPage getEnginePairPage(EngineVersion engine1, EngineVersion engine2) {
        HtmlPage page = HtmlPage.create("Comparison of " + engine1.toString() + " and " + engine2);
        page.addHeader(link(engine1) + " vs. " + link(engine2));

        {
            HtmlAdHocTable table = HtmlAdHocTable.create();
            table.addRow("Game", "Perf ratio", engine1 + " states per second", engine2 + " states per second");

            HtmlAdHocTable errorsTable = HtmlAdHocTable.create();
            errorsTable.addRow("Game", engine1 + " error message", engine2 + " error message");

            //Collect rows to sort and put in the tables
            Multimap<Double, List<String>> perfRows = HashMultimap.create();
            Multimap<Integer, List<String>> errorRows = HashMultimap.create();

            for (GameKey game : validGameKeys) {
                Map<EngineVersion, PerfTestResult> resultsByEngine = resultsByGame.get(game);
                if (resultsByEngine != null) {
                    PerfTestResult result1 = resultsByEngine.get(engine1);
                    PerfTestResult result2 = resultsByEngine.get(engine2);
                    if (result1 != null && result2 != null) {
                        int errorOrdering = 0;
                        if (!result1.wasSuccessful()) {
                            errorOrdering += 1;
                        }
                        if (!result2.wasSuccessful()) {
                            errorOrdering += 2;
                        }
                        if (errorOrdering == 0) {
                            //Success
                            double avg1 = getAvgNum(result1);
                            double avg2 = getAvgNum(result2);
                            double perfRatio = avg1 / avg2;
                            List<String> row = ImmutableList.of(link(game),
                                    Double.toString(perfRatio),
                                    Double.toString(avg1), Double.toString(avg2));
                            perfRows.put(perfRatio, row);
                        } else {
                            //At least one error
                            List<String> row = Lists.newArrayList();
                            row.add(link(game));
                            addErrorMessageForResult(row, result1);
                            addErrorMessageForResult(row, result2);

                            errorRows.put(errorOrdering, row);
                        }
                    }
                }
            }

            //Put rows in their tables in sorted order
            for (double key : ImmutableSortedSet.copyOf(perfRows.keySet())) {
                for (List<String> row : perfRows.get(key)) {
                    table.addRow(row);
                }
            }
            for (int key : ImmutableSortedSet.copyOf(errorRows.keySet())) {
                //TODO: Sort by game key here?
                for (List<String> row : errorRows.get(key)) {
                    errorsTable.addRow(row);
                }
            }


            page.addText("Game-by-game performance:");
            page.add(table);
            page.addText("Games with errors:");
            page.add(errorsTable);
        }
        return page;
    }

    private void addErrorMessageForResult(List<String> row, PerfTestResult result1) {
        if (result1.wasSuccessful()) {
            row.add("");
        } else {
            if (result1.getErrorMessage().isEmpty()) {
                row.add("unknown error");
            } else {
                row.add(result1.getErrorMessage());
            }
        }
    }

}
