package net.alloyggp.perf.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;

public class GameFiles {
    public static String read(File gameFile) throws IOException {
        return Files.readAllLines(gameFile.toPath())
                .stream()
                .reduce(new StringBuilder(),
                        (sb, string) -> sb.append(string),
                        (sb1, sb2) -> sb1.append(sb2.toString())).toString();
    }

    public static void write(String contents, File file) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(contents);
            out.flush();
        }
    }

    public static void write(Game game, File file) throws IOException {
        write(getCorrectlyFormattedRulesheet(game), file);
    }

    /**
     * game.getRulesheet() adds parentheses we don't want to the beginning and end
     */
    private static String getCorrectlyFormattedRulesheet(Game game) {
        StringBuilder sb = new StringBuilder();
        for (Gdl gdl : game.getRules()) {
            sb.append(gdl).append("\n");
        }
        return sb.toString();
    }
}
