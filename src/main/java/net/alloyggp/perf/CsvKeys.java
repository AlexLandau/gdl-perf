package net.alloyggp.perf;

public class CsvKeys {
	private CsvKeys() {
		//Not instantiable
	}

	//Used by error reports
	public static final String ERROR_MESSAGE = "errorMessage";

	//Used by perf reports
	public static final String MILLISECONDS_TAKEN = "millisecondsTaken";
	public static final String NUM_STATE_CHANGES = "numStateChanges";
	public static final String NUM_ROLLOUTS = "numRollouts";

}
