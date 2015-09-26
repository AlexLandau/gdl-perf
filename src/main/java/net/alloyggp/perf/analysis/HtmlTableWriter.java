package net.alloyggp.perf.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.io.Files;

public class HtmlTableWriter {
    public static void main(String[] args) throws IOException {
//        Table<String, String, String> table = MachineVsGameTableMaker.createForStat(result ->
//            Double.toString(1000 * result.getNumStateChanges() / (double) result.getMillisecondsTaken()));
        Table<String, String, String> table = PlaceCountTableMaker.create();

        writeTableToFile(table, new File("table.html"));
    }

    private static void writeTableToFile(Table<String, String, String> table,
            File file) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<html><head><title>Gdl-perf output</title></head>\n<body>\n");
        html.append("<table>\n");

        //TODO: Better ordering
        //Fix the column and row ordering
        List<String> columns = Lists.newArrayList(table.columnKeySet());
        Collections.sort(columns);
        List<String> rows = Lists.newArrayList(table.rowKeySet());
        Collections.sort(rows);
        //Add headers
        html.append("<tr><th/>");
        for (String col : columns) {
            html.append("<th>").append(makeUnderscoresLineBreakable(col)).append("</th>");
        }
        html.append("</tr>\n");

        //Add rows
        for (String row : rows) {
            html.append("<tr><th>");
            html.append(row);
            html.append("</th>");
            for (String col : columns) {
                html.append("<td>");
                html.append(Strings.nullToEmpty(table.get(row, col)));
                html.append("</td>");
            }
            html.append("</tr>\n");
        }


        html.append("</table>\n");
        html.append("</body></html>");

        Files.write(html, file, Charset.defaultCharset());
    }

    //This makes tables much more horizontally compact if EngineVersions are listed
    //along the top.
    private static String makeUnderscoresLineBreakable(String input) {
        //Add a zero-width space after the underscore
        return input.replaceAll("_", Matcher.quoteReplacement("_&#x200b;"));
    }


}
