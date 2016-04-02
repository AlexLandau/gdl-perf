package net.alloyggp.perf.gameanalysis;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ggp.base.util.game.Game;

import com.google.common.base.Preconditions;

import net.alloyggp.perf.io.GameFiles;
import net.alloyggp.perf.io.ResultFiles;

public class GameAnalysisProcess {
    public static void main(String[] args) throws IOException {
        Preconditions.checkArgument(args.length == 3);
        GameAnalysisTask task = GameAnalysisTask.valueOf(args[0]);
        File gameFile = new File(args[1]);
        File outputFile = new File(args[2]);

        String gameContents = GameFiles.read(gameFile);
        Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameContents));

        Map<String, String> results = task.runInThisProcess(game);

        ResultFiles.write(results, outputFile);
    }
}
