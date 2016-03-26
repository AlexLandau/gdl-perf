package net.alloyggp.perf.analysis.html;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

//@NotThreadSafe
public class HtmlList implements Htmlable {
    private final boolean numbered;
    private final List<String> contents = Lists.newArrayList();

    private HtmlList(boolean numbered, Collection<String> initialContents) {
        this.numbered = numbered;
        this.contents.addAll(initialContents);
    }

    public static Htmlable unnumbered(List<String> entries) {
        return new HtmlList(false, entries);
    }

    @Override
    public void addHtml(StringBuilder sb) {
        String tagName = numbered ? "ol" : "ul";
        sb.append("<").append(tagName).append(">\n");
        for (String element : contents) {
            sb.append("<li>").append(element).append("</li>\n");
        }
        sb.append("</").append(tagName).append(">\n");
    }

}
