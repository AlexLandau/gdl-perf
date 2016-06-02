package net.alloyggp.perf.io;

import java.util.List;

public interface Csvable {

    String getDelimiter();

    List<String> getValuesForCsv();

}
