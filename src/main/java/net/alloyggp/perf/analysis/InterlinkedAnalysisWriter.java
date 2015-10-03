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

import org.ggp.base.util.Pair;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
    //TODO: Immutify?
    private final Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame;
    private final ImmutableList<CorrectnessTestResult> allCorrectnessResults;

    public InterlinkedAnalysisWriter(ImmutableSet<GameKey> allGameKeys, ImmutableSet<GameKey> validGameKeys,
            ImmutableMap<GameKey, String> gameFilenames, ImmutableList<PerfTestResult> allPerfResults,
            Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame,
            ImmutableList<CorrectnessTestResult> allCorrectnessResults) {
        this.allGameKeys = allGameKeys;
        this.validGameKeys = validGameKeys;
        this.gameFilenames = gameFilenames;
        this.allPerfResults = allPerfResults;
        this.resultsByGame = resultsByGame;
        this.allCorrectnessResults = allCorrectnessResults;
    }

    private static InterlinkedAnalysisWriter create(Collection<GameKey> allGameKeys, Set<GameKey> validGameKeys,
            List<PerfTestResult> allPerfResults, Map<GameKey, Map<EngineVersion, PerfTestResult>> resultsByGame, List<CorrectnessTestResult> allCorrectnessResults) {
        BiMap<GameKey, String> gameFilenames = getGameFilenames(allGameKeys);
        return new InterlinkedAnalysisWriter(
                ImmutableSet.copyOf(allGameKeys),
                ImmutableSet.copyOf(validGameKeys),
                ImmutableMap.copyOf(gameFilenames),
                ImmutableList.copyOf(allPerfResults),
                resultsByGame,
                ImmutableList.copyOf(allCorrectnessResults));
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

        InterlinkedAnalysisWriter.create(allGameKeys,
                validGameKeys,
                allPerfResults,
                resultsByGame,
                allCorrectnessResults).writeAnalyses();
    }

    private void writeAnalyses() throws IOException {
        for (GameKey game : allGameKeys) {
            String htmlFilename = gameFilenames.get(game);
            writeHtmlPage(htmlFilename, getGamePage(game));
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
                SortedSet<Pair<EngineVersion, PerfTestResult>> resultPairs = Sets.newTreeSet(
                        Comparator.comparing((Pair<EngineVersion, PerfTestResult> pair) -> {
                            PerfTestResult result = pair.right;
                            return result.getNumStateChanges() / (double) result.getMillisecondsTaken();
                        }).reversed());
                for (Entry<EngineVersion, PerfTestResult> entry : resultsByEngine.entrySet()) {
                    resultPairs.add(Pair.from(entry));
                }
                HtmlAdHocTable engineTable = HtmlAdHocTable.create();
                for (Pair<EngineVersion, PerfTestResult> resultPair : resultPairs) {
                    engineTable.addRow(link(resultPair.left), getAvg(resultPair.right));
                }
                page.add(engineTable);
            }
        } else {
            page.addText("Game is considered invalid.");
        }
        return page;
    }

    private String getAvg(PerfTestResult result) {
        return Long.toString((result.getNumStateChanges() * 1000L) / result.getMillisecondsTaken());
    }

    private String link(EngineVersion engine) {
        // TODO: add actual link
        return engine.toString();
    }
}
