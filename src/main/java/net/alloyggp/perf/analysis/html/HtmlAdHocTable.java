package net.alloyggp.perf.analysis.html;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

//@NotThreadSafe
public class HtmlAdHocTable implements Htmlable {
    private final List<List<String>> headingRows = Lists.newArrayList();
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

    /**
     * Non-obvious behavior note: Heading rows always appear
     * before non-heading rows, regardless of the order they
     * are added. However, they are not included in any sorting
     * that is applied.
     */
    public void addHeadingRow(String... strings) {
        headingRows.add(Arrays.asList(strings));
    }

    @Override
    public void addHtml(StringBuilder sb) {
        sb.append("<table>\n");
        for (List<String> row : headingRows) {
            sb.append("<tr>");
            for (String cell : row) {
                //TODO: Escape cell contents
                sb.append("<th>")
                  .append(cell)
                  .append("</th>");
            }
            sb.append("</tr>\n");
        }
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

    /**
     * @param column the column number, zero-indexed
     */
    public void sortAlphabeticallyByColumn(int column) {
        rows.sort(Comparator.comparing(row -> row.get(column)));
    }
}
