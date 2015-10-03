package net.alloyggp.perf.analysis.html;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

//@NotThreadSafe
public class HtmlAdHocTable implements Htmlable {
    private final List<List<String>> rows = Lists.newArrayList();

    private HtmlAdHocTable() {
    }

    public static HtmlAdHocTable create() {
        return new HtmlAdHocTable();
    }

    public void addRow(String... strings) {
        rows.add(Arrays.asList(strings));
    }

    public void addRow(Iterable<String> strings) {
        rows.add(ImmutableList.copyOf(strings));
    }

    @Override
    public void addHtml(StringBuilder sb) {
        sb.append("<table>\n");
        for (List<String> row : rows) {
            sb.append("<tr>");
            for (String cell : row) {
                //TODO: Escape cell contents
                sb.append("<td>")
                  .append(cell)
                  .append("</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
    }
}
