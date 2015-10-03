package net.alloyggp.perf.analysis.html;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class HtmlTable implements Htmlable {
    private final Table<String, String, String> table;

    private HtmlTable(Table<String, String, String> table) {
        this.table = table;
    }

    @Override
    public void addHtml(StringBuilder sb) {
        sb.append("<table>\n");

        //TODO: Better ordering
        //Fix the column and row ordering
        List<String> columns = Lists.newArrayList(table.columnKeySet());
        Collections.sort(columns);
        List<String> rows = Lists.newArrayList(table.rowKeySet());
        Collections.sort(rows);
        //Add headers
        sb.append("<tr><th/>");
        for (String col : columns) {
            sb.append("<th>").append(makeUnderscoresLineBreakable(col)).append("</th>");
        }
        sb.append("</tr>\n");

        //Add rows
        for (String row : rows) {
            sb.append("<tr><th>");
            sb.append(row);
            sb.append("</th>");
            for (String col : columns) {
                sb.append("<td>");
                sb.append(Strings.nullToEmpty(table.get(row, col)));
                sb.append("</td>");
            }
            sb.append("</tr>\n");
        }


        sb.append("</table>\n");
    }

    //This makes tables much more horizontally compact if EngineVersions are listed
    //along the top.
    private static String makeUnderscoresLineBreakable(String input) {
        //Add a zero-width space after the underscore
        return input.replaceAll("_", Matcher.quoteReplacement("_&#x200b;"));
    }


}
