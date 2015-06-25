package net.alloyggp.perf;

import java.util.List;

import org.ggp.base.util.statemachine.Move;

public class ObservedError {
	private final List<List<String>> moveSelections;

	public static ObservedError create(String message,
			Object referenceResult,
			Object testResult) {

	}

	//TODO: Accept "ours", "theirs" as their own variables
	public static ObservedError create(String string,
			Object referenceResult,
			Object testResult,
			List<List<Move>> moveHistory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
	}
}
