package net.alloyggp.perf;

import java.util.List;

public interface Csvable {

	String getDelimiter();

	List<String> getValuesForCsv();

}
