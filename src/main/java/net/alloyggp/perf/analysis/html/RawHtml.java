package net.alloyggp.perf.analysis.html;

public class RawHtml implements Htmlable {
    private final String html;

    public RawHtml(String html) {
        this.html = html;
    }

    public static RawHtml create(String html) {
        return new RawHtml(html);
    }

    @Override
    public void addHtml(StringBuilder sb) {
        sb.append(html);
    }

}
