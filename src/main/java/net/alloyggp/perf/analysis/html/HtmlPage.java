package net.alloyggp.perf.analysis.html;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

//@NotThreadSafe
public class HtmlPage implements Htmlable {
    private final String title;
    private List<Htmlable> contents = Lists.newArrayList();

    public HtmlPage(String title) {
        this.title = title;
    }

    public static HtmlPage create(String title) {
        return new HtmlPage(title);
    }

    public void add(Htmlable content) {
        contents.add(content);
    }

    public void addHeader(String header) {
        addHeader(header, 1);
    }

    private void addHeader(String header, int level) {
        Preconditions.checkArgument(level >= 1 && level <= 6);
        //TODO: Escape header
        contents.add(RawHtml.create("<h"+level+">"+header+"</h"+level+">\n"));
    }

    @Override
    public void addHtml(StringBuilder sb) {
        sb.append("<html><head><title>")
          .append(title)
          .append("</title></head>\n")
          .append("<body>\n");
        for (Htmlable content : contents) {
            content.addHtml(sb);
        }
        sb.append("</body></html>\n");
    }

    public void addText(String text) {
        //TODO: Escape text
        contents.add(RawHtml.create("<p>"+text+"</p>\n"));
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        addHtml(sb);
        return sb.toString();
    }
}
